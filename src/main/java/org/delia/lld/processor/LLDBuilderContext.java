package org.delia.lld.processor;

import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.hld.dat.DatService;
import org.delia.relation.RelationInfo;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.HashMap;
import java.util.Map;

//--builder
public class LLDBuilderContext {
    public DTypeRegistry registry;
    public ScalarValueBuilder valueBuilder;
    public DatService datSvc;
    Map<String, RelationInfo> assocMap = new HashMap<>();
    public DBType dbType;
    public AST.Loc loc; //set by builder for each ast statement

    public boolean isMEMDb() { return DBType.MEM.equals(dbType); }
}
