package org.delia.migrationparser;

import org.delia.type.DStructType;
import org.delia.type.OrderedMap;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationContext {
    public List<DStructType> doomedL = new ArrayList<>();
    public Map<String, String> renamedTypeMap = new HashMap<>();
    public Map<String, OrderedMapEx> changeFieldMap = new HashMap<>(); //add or change
    public MigrationFieldResult migrationFieldResult = new MigrationFieldResult();
}
