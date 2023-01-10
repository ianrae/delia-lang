package org.delia.hld.dat;

import org.delia.compiler.ast.Exp;
import org.delia.relation.RelationInfo;

import java.util.List;

//TODO add dat-id table to store this. currently we use in-memory approach which won't work after a few migrations
public interface DatService {
    Integer findDat(Exp.JoinInfo joinInfo);

    Integer findDat(RelationInfo relinfo);

    //        Integer findAssocInfo(Exp.JoinInfo joinInfo);
    AssocSpec findAssocInfo(RelationInfo relinfo);
    AssocSpec findByAssocTableName(String assocTableName);
    List<AssocSpec> findAll();
    int findMaxDatId();
}
