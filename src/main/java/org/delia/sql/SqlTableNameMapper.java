package org.delia.sql;

import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.rule.DRule;
import org.delia.rule.rules.SqlTableNameRule;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SqlTableNameMapper {
    private DeliaLog log;
    private Map<String, String> tableNameMap = new ConcurrentHashMap<>();

    public SqlTableNameMapper(DeliaLog log) {
        this.log = log;
    }

    public void prepare(List<LLD.LLStatement> lldStatements) {
        for (LLD.LLStatement stmt : lldStatements) {
            if (stmt instanceof LLD.HasLLTable) {
                LLD.HasLLTable stmtWithTable = (LLD.HasLLTable) stmt;
                LLD.LLTable llTable = stmtWithTable.getTable();
                processTable(llTable);
            }

            if (stmt instanceof LLD.LLSelect) {
                LLD.LLSelect llSelect = (LLD.LLSelect) stmt;
                for (LLD.LLEx llex : llSelect.fields) {
                    doLLEx(llex);
                }
                for (LLD.LLJoin llJoin : llSelect.joinL) {
                    doJoin(llJoin);
                }
                for (LLD.LLEx llex : llSelect.finalFieldsL) {
                    doLLEx(llex);
                }
            }

            if (stmt instanceof LLD.LLBulkInsert) {
                LLD.LLBulkInsert bulk = (LLD.LLBulkInsert) stmt;
                for (LLD.LLInsert insertStmt : bulk.insertStatements) {
                    LLD.LLTable llTable = insertStmt.getTable();
                    processTable(llTable);
                }
            }

            if (stmt instanceof LLD.LLCreateTable) {
                LLD.LLCreateTable llCreateTable = (LLD.LLCreateTable) stmt;
                processTable(llCreateTable.table);
                for (LLD.LLField llField : llCreateTable.fields) {
                    doLLEx(llField);
                }

            }
        }
    }

    private void processTable(LLD.LLTable llTable) {
        Optional<String> tableNameToUse = getFromRule(llTable);
        if (tableNameToUse.isPresent()) {
            String defaultTableName = llTable.formatter.formatName(llTable.physicalType.getName()); //without schema
            tableNameMap.put(defaultTableName, tableNameToUse.get());
            log.logDebug("SQL-TABLE-NAME %s -> %s", defaultTableName, tableNameToUse.get());
            llTable.sqlTableNameToUse = tableNameToUse.get();
        }
    }

    private void doLLEx(LLD.LLEx llex) {
        if (llex instanceof LLD.LLFieldValue) {
            LLD.LLFieldValue fv = (LLD.LLFieldValue) llex;
            processTable(fv.field.physicalTable);
        } else if (llex instanceof LLD.LLFinalFieldEx) {
            LLD.LLFinalFieldEx ffex = (LLD.LLFinalFieldEx) llex;
            processTable(ffex.physicalTable);
        } else if (llex instanceof LLD.LLField) {
            LLD.LLField ffex = (LLD.LLField) llex;
            processTable(ffex.physicalTable);
        }
    }

    private void doJoin(LLD.LLJoin llJoin) {
        processTable(llJoin.physicalLeft.physicalTable);
        processTable(llJoin.physicalRight.physicalTable);
    }


    private Optional<String> getFromRule(LLD.LLTable llTable) {
        Optional<DRule> opt = llTable.physicalType.getRawRules().stream().filter(x -> x instanceof SqlTableNameRule).findAny();
        if (opt.isPresent()) {
            SqlTableNameRule rule = (SqlTableNameRule) opt.get();
            return Optional.of(rule.getTableName());
        } else {
            return Optional.empty();
        }
    }

    public String getSQLTableName(LLD.LLTable llTable) {
        return llTable.getSQLName();
    }
}
