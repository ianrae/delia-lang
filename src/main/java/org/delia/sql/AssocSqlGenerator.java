package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.tok.TokWhereClauseUtils;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.dbimpl.mem.impl.QueryType;

public class AssocSqlGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;
    private final DatService datSvc;

    public AssocSqlGenerator(FactoryService factorySvc, SqlValueRenderer sqlValueRenderer, ScalarValueBuilder valueBuilder, DatService datSvc) {
        super(factorySvc);
        this.sqlValueRenderer = sqlValueRenderer;
        this.valueBuilder = valueBuilder;
        this.datSvc = datSvc;
    }

    public SqlStatement renderInsertSubQuery(LLD.LLInsert statement) {
        /*     insert into customeraddressdat1 (leftv, rightv)
                    select id,100 from customer;
         */

        StrCreator sc = new StrCreator();
        sc.o("INSERT INTO %s (leftv,rightv) ", statement.getTableName());
        LLD.LLSubQueryInsertInfo subQueryInfo = statement.subQueryInfo;
        LLD.LLFieldValue fieldVal1 = subQueryInfo.subQueryFieldL.get(0);
        LLD.LLFieldValue fieldVal2 = subQueryInfo.subQueryFieldL.get(1);
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(subQueryInfo.subQueryStructType);
        AssocSpec assocSpec = datSvc.findAssocInfo(subQueryInfo.relinfo);
        String fieldName = null;
        DValue value = null;
        DType valueType = null;
        if (fieldVal1.dval == null && fieldVal1.dvalList == null) {
            fieldName = pkpair.name; //fieldVal1.field.getFieldName();
        } else {
            value = fieldVal1.dval;
            valueType = fieldVal1.field.physicalPair.type;
        }

        if (fieldVal2.dval == null && fieldVal2.dvalList == null) {
            fieldName = pkpair.name; //fieldVal2.field.getFieldName();
        } else {
            value = fieldVal2.dval;
            valueType = fieldVal1.field.physicalPair.type;
        }

        SqlStatement sqlStatement = new SqlStatement();
        DValue realVal = this.sqlValueRenderer.noRenderSqlParam(value, valueType, sqlStatement.typeHintL);
        sqlStatement.paramL.add(realVal);

        boolean isFlipped = assocSpec.isFlipped(subQueryInfo.relinfo);
        String selectFromType = subQueryInfo.subQueryStructType.getName();
        if (isFlipped) {
            //               100,id
            sc.o(" select ?,%s from %s", fieldName, selectFromType);
        } else {
            //                id,100
            sc.o(" select %s,? from %s", fieldName, selectFromType);
        }
        if (subQueryInfo.whereTok != null && QueryType.PRIMARY_KEY.equals(subQueryInfo.queryType)) {
            DValue pkval = TokWhereClauseUtils.extractPKWhereClause(subQueryInfo.whereTok);
            pkval = this.sqlValueRenderer.noRenderSqlParam(pkval, valueType, sqlStatement.typeHintL);
            sqlStatement.paramL.add(pkval);
            sc.o(" WHERE %s=?", pkpair.name);
        }

        sc.o(";");

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }
}
