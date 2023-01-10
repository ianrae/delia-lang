package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.dbimpl.mem.impl.QueryType;
import org.delia.dbimpl.mem.impl.QueryTypeDetector;
import org.delia.hld.CrudAction;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokWhereClauseUtils;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AssocLLDGeneratorImpl extends LLDProcessorBase implements AssocLLDGenerator {

    public AssocLLDGeneratorImpl(FactoryService factorySvc, DatService datSvc) {

        super(factorySvc, datSvc);
    }


    @Override
    public Collection<? extends LLD.LLStatement> convertToUpdates(HLD.HLDUpdateUpsertBase hldStatement, List<LLD.LLInsert> assocInserts, LLDBuilderContext ctx) {
        List<LLD.LLStatement> statements = new ArrayList<>();
        QueryType queryType = calcQueryType(hldStatement.whereTok);

        int index = 0;
        for (LLD.LLInsert llInsert : assocInserts) {
            switch (queryType) {
                case ALL_ROWS:
                    genUpdateAssocAllRows(hldStatement, llInsert, statements, ctx);
                    break;
                case PRIMARY_KEY:
                    genUpdateAssocPK(hldStatement, llInsert, statements, (index == 0), ctx);
                    break;
                case OP:
//TODO                    break;
                default:
                    DeliaExceptionHelper.throwNotImplementedError("update: unsupported queryType %s", queryType.name());
                    break;
            }
            index++;
        }
        return statements;
    }

    @Override
    public void genUpdateAssocAllRows(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, LLDBuilderContext ctx) {
        if (isAnyCrudAction(llInsert)) {
            DeliaExceptionHelper.throwNotImplementedError("crud-action MM all rows");
        }

        //delete CustomerAddressDat1p[true]
        LLD.LLDelete llDelete = new LLD.LLDelete(hldStatement.getLoc());
        llDelete.table = llInsert.table;
//        llDelete.whereClause = WhereClauseUtils.buildTrueWhereClause(ctx.valueBuilder);
        llDelete.whereTok = TokWhereClauseUtils.buildTrueWhereClause(ctx.valueBuilder);
        statements.add(llDelete);

        //insert into CDat1 (leftv, rightv)
        //select id,100 from customer
        LLD.LLInsert insertWithSubQuery = new LLD.LLInsert(hldStatement.getLoc());
        insertWithSubQuery.table = llInsert.table;
        insertWithSubQuery.subQueryInfo = new LLD.LLSubQueryInsertInfo(llInsert.fieldL, hldStatement.hldTable);
        Optional<HLD.HLDFieldValue> hldFieldVal = hldStatement.fields.stream().filter(x -> x.hldField.relinfo != null).findAny();
        if (hldFieldVal.isPresent()) { //should always be one
            insertWithSubQuery.subQueryInfo.relinfo = hldFieldVal.get().hldField.relinfo;
        }

        statements.add(insertWithSubQuery);
    }

    @Override
    public void genUpdateAssocPK(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, boolean isFirst, LLDBuilderContext ctx) {
        //delete CustomerAddressDat1p[true]
        DValue pkval = WhereClauseUtils.extractPKWhereClause(hldStatement.whereClause);
        if ((isNoCrudAction(llInsert) && isFirst) || isCrudAction(llInsert, CrudAction.UPDATE) || isCrudAction(llInsert, CrudAction.DELETE)) {
            LLD.LLDelete llDelete = new LLD.LLDelete(hldStatement.getLoc());
            llDelete.table = llInsert.table;

            RelationInfo relinfo = llInsert.assocUpdateRelinfo;
            Exp.JoinInfo joinInfo = new Exp.JoinInfo(relinfo.nearType.getTypeName(),
                    relinfo.farType.getTypeName(),
                    relinfo.fieldName);
            joinInfo.relinfo = relinfo;
            AssocSpec assocSpec = datSvc.findAssocInfo(relinfo);
            String fieldName = assocSpec.isFlipped(relinfo) ? "rightv" : "leftv";
            llDelete.whereTok = TokWhereClauseUtils.buildEqWhereClause(fieldName, pkval, joinInfo);
            statements.add(llDelete);
        }

        if (isNoCrudAction(llInsert) || isCrudAction(llInsert, CrudAction.UPDATE) || isCrudAction(llInsert, CrudAction.INSERT)) {
            //TODO: can replace with simple insert CustomerAddressDat1 (leftv,rightv) (55,100)
            //insert into CDat1 (leftv, rightv)
            //select id,100 from customer where id=55
            LLD.LLInsert llInsertAssoc = new LLD.LLInsert(hldStatement.getLoc());
            llInsertAssoc.table = llInsert.table;
            llInsertAssoc.fieldL = llInsert.fieldL;
            for (LLD.LLFieldValue fieldVal : llInsertAssoc.fieldL) {
                if (fieldVal.dval == null && fieldVal.dvalList == null) {
                    fieldVal.dval = pkval;
                    break;
                }
            }

            statements.add(llInsertAssoc);
        }
    }

    private boolean isCrudAction(LLD.LLInsert llInsert, CrudAction crudAction) {
        return crudAction.equals(llInsert.fieldL.get(0).field.crudAction); //any of them
    }

    private boolean isNoCrudAction(LLD.LLInsert llInsert) {
        return !isAnyCrudAction(llInsert);
    }

    private boolean isAnyCrudAction(LLD.LLInsert llInsert) {
        return llInsert.fieldL.get(0).field.crudAction != null; //any of them
    }

    //TODO
    @Override
    public void genUpdateAssocFullQuery(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, LLDBuilderContext ctx) {
        //delete CustomerAddressDat1p[true]
        LLD.LLDelete llDelete = new LLD.LLDelete(hldStatement.getLoc());
        llDelete.table = llInsert.table;
        llDelete.whereTok = TokWhereClauseUtils.buildTrueWhereClause(ctx.valueBuilder);
        statements.add(llDelete);

        //TODO: can replace with simple insert CustomerAddressDat1 (leftv,rightv) (55,100)
        //insert into CDat1 (leftv, rightv)
        //select id,100 from customer where id=55
        LLD.LLInsert insertWithSubQuery = new LLD.LLInsert(hldStatement.getLoc());
        insertWithSubQuery.table = llInsert.table;
        insertWithSubQuery.subQueryInfo = new LLD.LLSubQueryInsertInfo(llInsert.fieldL, hldStatement.hldTable);
        Optional<HLD.HLDFieldValue> hldFieldVal = hldStatement.fields.stream().filter(x -> x.hldField.relinfo != null).findAny();
        if (hldFieldVal.isPresent()) { //should always be one
            insertWithSubQuery.subQueryInfo.relinfo = hldFieldVal.get().hldField.relinfo;
        }
        DValue pkval = WhereClauseUtils.extractPKWhereClause(hldStatement.whereClause);
        insertWithSubQuery.subQueryInfo.whereTok = TokWhereClauseUtils.buildPKWhereClause(ctx.valueBuilder, pkval.asString());
        insertWithSubQuery.subQueryInfo.queryType = QueryType.PRIMARY_KEY;

        statements.add(insertWithSubQuery);
    }

    private QueryType calcQueryType(Tok.WhereTok whereClause) {
        QueryTypeDetector detector = new QueryTypeDetector();
        return detector.detectQueryType(whereClause, null);
    }


    @Override
    public void build(HLD.HLDStatement hldStatement, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        DeliaExceptionHelper.throwError("dont-call-this", "this method should not be used");
    }

}
