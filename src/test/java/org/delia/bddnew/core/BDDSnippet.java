package org.delia.bddnew.core;

import org.delia.db.DBType;

import java.util.ArrayList;
import java.util.List;

public class BDDSnippet {
    public DBType dbType;
    public SnippetType type;
    public List<String> lines = new ArrayList<>();
    public String thenType; //only used by value snippet
    public boolean bulkInsertEnabled;
}
