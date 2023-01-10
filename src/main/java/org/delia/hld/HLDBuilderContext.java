package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.db.DBType;
import org.delia.error.ErrorTracker;
import org.delia.hld.dat.DatService;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.Map;

public class HLDBuilderContext {
    public DTypeRegistry registry;
    public String currentSchema;
    public ScalarValueBuilder valueBuilder;
    public DatService datSvc;
    public ErrorTracker localET;
    public DBType dbType;
    public Map<String, String> syntheticIdMap;
    public AST.Loc loc; //set by HLDBuilder for each ast statement

    public boolean isMEMDb() { return DBType.MEM.equals(dbType); }
}
