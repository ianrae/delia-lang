package org.delia.bddnew.core;

import org.delia.DeliaSession;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.error.DeliaError;
import org.delia.log.DeliaLog;
import org.delia.type.*;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ValuesSnippetRunner implements SnippetRunner {
    private final DeliaLog log;
    private ConnectionProvider connProvider;

    public ValuesSnippetRunner(DeliaLog log) {
        this.log = log;
    }

    @Override
    public void setConnectionProvider(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    @Override
    public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
        BDDSnippetResult res = new BDDSnippetResult();
        res.sess = previousRes == null ? null : previousRes.sess;

        List<String> valueL = new ArrayList<>();
        List<DValue> dvalList = new ArrayList<>();
        List<DeliaError> errorList = new ArrayList<>();
        DateChecker dateChecker = null;
        if (previousRes != null && previousRes.resValue != null) {
            if (!previousRes.resValue.errors.isEmpty()) {
                errorList.addAll(previousRes.resValue.errors);
//                String tmp = "ERROR: " + previousRes.resValue.errors.get(0).toString(); //TODO do we need to handle more than one?
//                valueL.add(tmp);
            } else {
                dvalList = previousRes.resValue.getAsDValueList();
                if (dvalList == null) {
                    dvalList = Collections.singletonList(previousRes.resValue.getAsDValue());
                }

                if (dvalList.isEmpty()) {
                    valueL.add("null");
                } else {
                    for (DValue dval : dvalList) {
                        valueL.addAll(generateFromDVal(dval, previousRes.sess));
                    }
                }
            }

            //handle single line version of bdd: let x boolean = false;false
            if (snippet.lines.size() == 1 && ! dvalList.isEmpty()) {
                String line = snippet.lines.get(0).trim();
                if (line.startsWith("date(")) {
                    dateChecker = new DateChecker();
                } else if (! line.contains("value:") && ! line.toLowerCase(Locale.ROOT).contains("error:")) {
                    DValue dval = dvalList.get(0);
                    snippet.lines.clear();
                    if (dval == null) {
                        snippet.lines.add("value:a:x:null");
                    } else {
                        String typeStr = BuiltInTypes.convertDTypeNameToDeliaName(dval.getType().getName());
                        String tmp = adjustQuotes(line, dval);
                        line = String.format("value:a:%s:%s", typeStr, tmp);
                        snippet.lines.add(line);
                    }
                }
            }
        }


        ThenValue thenVal = new ThenValue(snippet.lines);
        if (! errorList.isEmpty()) {
            StructChecker checker = new StructChecker();
            res.ok = checker.compareError(thenVal, errorList, log);
        } else if (dateChecker != null) {
            DeliaSession sess = previousRes == null ? null : previousRes.sess;;
            thenVal.expected = snippet.lines.get(0);
            res.ok = dateChecker.compareObj(thenVal, dvalList.get(0), log, sess);
        } else {
            StructChecker checker = new StructChecker();
            if (previousRes != null && previousRes.ok) {
                res.ok = checker.compareMultiObj(thenVal, valueL, log);
            } else if (previousRes != null) {
//            res.ok = checker.compareError(thenVal, previousRes.errors, log);
                res.ok = checker.compareMultiObj(thenVal, valueL, log);
//        } else {
//            res.ok = checker.compareError(thenVal, res.errors, log);
            }
        }

        return res;
    }

    private String adjustQuotes(String line, DValue dval) {
        String tmp = line.trim();
        //strings in bdd must use ' delim. eg 'abc' unless string contains '
        if (dval != null && dval.getType().isShape(Shape.STRING)) {
            if (tmp.startsWith("'") && tmp.endsWith("'")) {
                //do nothing
            } else if (tmp.startsWith("\"") && tmp.endsWith("\"")) {
                if (tmp.contains("'")) {
                    //hmm we render "a'b" as 'a'b'. not correct but is just for bdd
                    tmp = tmp.substring(1, tmp.length() - 1); //remove "
                    tmp = String.format("'%s'", tmp);
                } else {
                    tmp = tmp.substring(1, tmp.length() - 1); //remove "
                    tmp = String.format("'%s'", tmp);
                }
            }
        }
        return tmp;
    }

    private List<String> generateFromDVal(DValue dval, DeliaSession sess) {
        if (dval == null) {
            return Collections.singletonList("value:a:x:null");
        }
        uglyFixForFetchedItems(dval);

        SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
        gen.truncateLargeBlob = false;
        // ErrorTracker et = new SimpleErrorTracker(log);

//        DeliaGeneratePhase phase = sess.getExecutionContext().generator;
        DeliaGeneratePhase phase = new DeliaGeneratePhase(sess.getDelia().getFactoryService(), sess.getRegistry());

        boolean b = phase.generateValue(gen, dval, "a");
        if (! b) {
            log.log("sdf phase.generateValue");
        }
        assertEquals(true, b);
        return gen.outputL;
    }

    private void uglyFixForFetchedItems(DValue dval) {
        if (! dval.getType().isStructShape()) return;
        //bug in delia. sometimes a DRelation will have 2 keys and 3 fetched items.
        DStructType structType = dval.asStruct().getType();
        for (TypePair pair : structType.getAllFields()) {
            if (pair.type.isStructShape()) {
                DValue inner = dval.asStruct().getField(pair.name);
                if (inner == null) {
                    continue;
                }
                DRelation drel = inner.asRelation();
                if (drel.haveFetched() && drel.getFetchedItems().size() > drel.getMultipleKeys().size()) {
                    this.log.log("ugly haccccccccccccccccck");
                    List<DValue> newFetchedL = new ArrayList<>();
                    for (DValue fkval : drel.getMultipleKeys()) {
                        List<DValue> tmpL = drel.getFetchedItems().stream().filter(vv -> isPKMatch(vv, fkval)).collect(Collectors.toList());
                        if (tmpL.size() == 1) {
                            newFetchedL.add(tmpL.get(0));
                        } else {
                            newFetchedL.add(pickBestOne(tmpL));
                        }
                    }
                    drel.setFetchedItems(newFetchedL);
                }

            }
        }
    }

    private DValue pickBestOne(List<DValue> tmpL) {
//        DType typeBeingFixed = null; //TODO: fix better later. this impl can only handle one drel inside each dval
        DValue dvalWithMaxFKs = null;
        int numFKsSeen = -1;

        for (DValue dval : tmpL) {
            DStructType structType = dval.asStruct().getType();
            for (TypePair pair : structType.getAllFields()) {
                if (pair.type.isStructShape()) {
                    DValue inner = dval.asStruct().getField(pair.name);
                    DRelation drel = inner.asRelation();
                    if (drel.getMultipleKeys().size() > numFKsSeen) {
                        numFKsSeen = drel.getMultipleKeys().size();
                        dvalWithMaxFKs = dval;
                    }
                }
            }
        }
        return dvalWithMaxFKs;
    }

    private boolean isPKMatch(DValue vv, DValue fkval) {
        DValue vvpk = DValueHelper.findPrimaryKeyValue(vv);
        String s1 = vvpk.asString();
        String s2 = fkval.asString();
        return s1.equals(s2);
    }

}
