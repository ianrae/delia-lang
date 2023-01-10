package org.delia.bddnew.core;

import org.delia.DeliaSession;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.log.Log;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ValuesSnippetRunner implements SnippetRunner {
    private final Log log;
    private ConnectionProvider connProvider;

    public ValuesSnippetRunner(Log log) {
        this.log = log;
    }

    @Override
    public void setConnectionProvider(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    @Override
    public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
        BDDSnippetResult res = new BDDSnippetResult();

        List<String> valueL = new ArrayList<>();
        if (previousRes != null && previousRes.resValue != null) {
            List<DValue> dvalList = previousRes.resValue.getAsDValueList();
            for (DValue dval : dvalList) {
                valueL.addAll(generateFromDVal(dval, previousRes.sess));
            }
        }

//            for(String s: valueL) {
//                log.log("x: " + s);
//            }
        ThenValue thenVal = new ThenValue(snippet.lines);
        StructChecker checker = new StructChecker();
        if (previousRes != null && previousRes.ok) {
            res.ok = checker.compareMultiObj(thenVal, valueL, log);
        } else if (previousRes != null) {
            res.ok = checker.compareError(thenVal, previousRes.errors, log);
//        } else {
//            res.ok = checker.compareError(thenVal, res.errors, log);
        }

        return res;
    }

    private List<String> generateFromDVal(DValue dval, DeliaSession sess) {
        uglyFixForFetchedItems(dval);
        SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
        gen.truncateLargeBlob = false;
        // ErrorTracker et = new SimpleErrorTracker(log);
        DeliaGeneratePhase phase = sess.getExecutionContext().generator;
        boolean b = phase.generateValue(gen, dval, "a");
        assertEquals(true, b);
        return gen.outputL;
    }

    private void uglyFixForFetchedItems(DValue dval) {
        //bug in delia. sometimes a DRelation will have 2 keys and 3 fetched items.
        DStructType structType = dval.asStruct().getType();
        for(TypePair pair: structType.getAllFields()) {
            if (pair.type.isStructShape()) {
                DValue inner = dval.asStruct().getField(pair.name);
                if (inner == null) {
                    continue;
                }
                DRelation drel = inner.asRelation();
                if (drel.haveFetched() && drel.getFetchedItems().size() > drel.getMultipleKeys().size()) {
                    this.log.log("ugly haccccccccccccccccck");
                    List<DValue> newFetchedL = new ArrayList<>();
                    for(DValue fkval: drel.getMultipleKeys()) {
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

        for(DValue dval: tmpL) {
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
