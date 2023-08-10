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
                Optional<String> tableNameToUse = getFromRule(llTable);
                if (tableNameToUse.isPresent()) {
                    String defaultTableName = llTable.formatter.formatName(llTable.physicalType.getName()); //without schema
                    tableNameMap.put(defaultTableName, tableNameToUse.get());
                    log.logDebug("SQL-TABLE-NAME %s -> %s", defaultTableName, tableNameToUse.get());
                    llTable.sqlTableNameToUse = tableNameToUse.get();
                }
            }
        }
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
