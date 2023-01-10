package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeliaExecutable {
    public DBType dbType;
    public AST.DeliaScript script;
    public DTypeRegistry registry;
    public List<HLD.HLDStatement> hldStatements = new ArrayList<>();
    public List<LLD.LLStatement> lldStatements = new ArrayList<>();
    public List<SqlStatement> sqlStatements = new ArrayList<>();
//    public Map<String, ResultValue> varMap = new ConcurrentHashMap<>();
    public DatService datSvc;
    public boolean inTransaction;
}
