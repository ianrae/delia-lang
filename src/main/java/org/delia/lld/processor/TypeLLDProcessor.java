package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;

import java.util.List;
import java.util.Map;

public class TypeLLDProcessor implements LLDProcessor {
    private final DatService datSvc;

    public TypeLLDProcessor(DatService datSvc) {
        this.datSvc = datSvc;
    }

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.TypeHLDStatement statement = (HLD.TypeHLDStatement) hldStatementParam;
        LLD.LLTable llTable = new LLD.LLTable(statement.hldTable, statement.hldTable, new LLD.DefaultLLNameFormatter());
//            llTable.alias;
//            public int datId; //0 means not an assoc table

        LLD.LLCreateTable llCreateTable = new LLD.LLCreateTable(hldStatementParam.getLoc());
        llCreateTable.table = llTable;

        Map<String, RelationInfo> assocMap = ctx.assocMap;
        for (HLD.HLDField hldField : statement.fields) {
            LLD.LLField field = new LLD.LLField(hldField.pair, llTable, new LLD.DefaultLLNameFormatter());
            llCreateTable.fields.add(field);

            //add assoc
            if (hldField.relinfo != null && hldField.relinfo.isManyToMany()) {
                //relinfo can exist on both sides of a relation. Customer.addr and Address.cust
                String key1 = generateAssocKey1(hldField.relinfo);
                String key2 = generateAssocKey1(hldField.relinfo.otherSide);
                if (!assocMap.containsKey(key1) && !assocMap.containsKey(key2)) {
                    assocMap.put(key1, hldField.relinfo);
                }
            }
        }

        lldStatements.add(llCreateTable);
    }


    private String generateAssocKey1(RelationInfo relinfo) {
//        if (relinfo == null) return null;
        String key = String.format("%s:%s:%s", relinfo.nearType.getName(), relinfo.farType.getName(), relinfo.fieldName);
        return key;
    }
}
