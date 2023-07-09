package org.delia.runner.bulkinsert;

import org.delia.DeliaOptions;
import org.delia.lld.LLD;
import org.delia.util.StrCreator;
import org.delia.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BulkInsertBuilder {
    private final DeliaOptions deliaOptions;

    public BulkInsertBuilder(DeliaOptions deliaOptions) {
        this.deliaOptions = deliaOptions;
    }

    public List<LLD.LLStatement> process(List<LLD.LLStatement> statements) {
        if (!deliaOptions.bulkInsertEnabled) {
            return statements; //do nothing
        }

        List<Object> outList = buildListWithSpans(statements);

        List<LLD.LLStatement> resultL = processHolders(outList);
        return resultL;
    }

    //contiguous insert statements gathered into a spanholder object
    private List<Object> buildListWithSpans(List<LLD.LLStatement> statements) {
        boolean inRun = false;

        List<Object> outList = new ArrayList<>();
        for (LLD.LLStatement statement : statements) {
            if (statement instanceof LLD.LLInsert) {
                LLD.LLInsert llInsert = (LLD.LLInsert) statement;
                if (!inRun) {
                    inRun = true;
                    SpanHolder holder = new SpanHolder();
                    holder.statements.add(llInsert);
                    outList.add(holder);
                } else {
                    SpanHolder holder = (SpanHolder) outList.get(outList.size() - 1);
                    holder.statements.add(llInsert);
                }
            } else {
                if (inRun) {
                    inRun = false;
                }
                outList.add(statement);
            }
        }
        return outList;
    }

    //replace each span with
    //(a) one or more LLBulkInserts
    //(b) its contents (LLInserts)
    //(c) mixture of (a) or (b)
    private List<LLD.LLStatement> processHolders(List<Object> list) {
        List<LLD.LLStatement> finalList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof SpanHolder) {
                List<LLD.LLStatement> tmpL = processSpan((SpanHolder) obj);
                finalList.addAll(tmpL);
            } else {
                LLD.LLStatement stmt = (LLD.LLStatement) obj;
                finalList.add(stmt);
            }
        }

        return finalList;
    }

    private List<LLD.LLStatement> processSpan(SpanHolder obj) {
        List<LLD.LLStatement> resultL = new ArrayList<>();
        LLD.LLInsert candidate = null;
        LLD.LLBulkInsert bulkInsert = null;

        for (LLD.LLInsert stmt : obj.statements) {
            if (candidate == null) {
                candidate = stmt;
                bulkInsert = new LLD.LLBulkInsert(stmt.getLoc());
                bulkInsert.first = stmt;
                bulkInsert.insertStatements.add(stmt);
            } else {
                if (isMatch(candidate, stmt)) {
                    bulkInsert.insertStatements.add(stmt);
                } else {
                    addBulkIfNeeded(bulkInsert, resultL);

                    candidate = stmt;
                    bulkInsert = new LLD.LLBulkInsert(stmt.getLoc());
                    bulkInsert.first = stmt;
                    bulkInsert.insertStatements.add(stmt);
                }
            }
        }

        addBulkIfNeeded(bulkInsert, resultL);
        return resultL;
    }

    private boolean isMatch(LLD.LLInsert candidate, LLD.LLInsert stmt) {
        if (candidate.subQueryInfo != null || stmt.subQueryInfo != null) {
            return false;
        }
        String fingerprint = buildFingerprint(candidate);
        String fingerprint2 = buildFingerprint(stmt);
        return fingerprint.equals(fingerprint2);
    }

    //TODO: do we need to include fields in fingerprint?
    private String buildFingerprint(LLD.LLInsert candidate) {
        StrCreator sc = new StrCreator();
        sc.o("%s;", candidate.getTableName());
        List<String> nameL = candidate.fieldL.stream().map(x -> x.field.getFieldName()).collect(Collectors.toList());
        sc.addStr(StringUtil.flatten(nameL));
        return sc.toString();
    }

    private void addBulkIfNeeded(LLD.LLBulkInsert bulkInsert, List<LLD.LLStatement> resultL) {
        if (bulkInsert != null) {
            if (bulkInsert.insertStatements.size() == 1) { //just one? not a bulk insert
                resultL.add(bulkInsert.insertStatements.get(0));
            } else {
                resultL.add(bulkInsert);
            }
        }
    }


}
