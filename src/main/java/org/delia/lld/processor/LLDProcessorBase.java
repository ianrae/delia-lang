package org.delia.lld.processor;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dbimpl.mem.impl.InQueryTypeDetails;
import org.delia.dbimpl.mem.impl.QueryType;
import org.delia.dbimpl.mem.impl.QueryTypeDetector;
import org.delia.hld.CrudAction;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.tok.TokClauseBuilder;
import org.delia.tok.TokWhereClauseUtils;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LLDProcessorBase extends ServiceBase implements LLDProcessor {

    protected final DatService datSvc;
    protected final TokClauseBuilder tokBuilder;

    public LLDProcessorBase(FactoryService factorySvc, DatService datSvc) {
        super(factorySvc);
        this.datSvc = datSvc;
        this.tokBuilder = new TokClauseBuilder(factorySvc);
    }

    protected List<LLD.LLFieldValue> createFields(LLD.LLTable llTable, List<HLD.HLDFieldValue> fields, List<LLD.LLInsert> assocInserts,
                                                  List<LLD.LLDelete> assocDeletes, HLD.HLDFieldValue fieldPK, LLDBuilderContext ctx) {
        List<LLD.LLFieldValue> lldFields = new ArrayList<>();
        for (HLD.HLDFieldValue hldField : fields) {
            LLD.LLField field = new LLD.LLField(hldField.hldField.pair, llTable, new LLD.DefaultLLNameFormatter());
            field.crudAction = hldField.hldField.crudAction;

            //the fieldL is all the logical fields involved.
            //Because of ManyToMany relations, some of those fields may be in an assoc table, not in the statement's table
            field.isAssocField = LLDUtils.isAssocField(field);
            if (field.isAssocField) {
                assocInserts.addAll(createAssocInsert(hldField, field, ctx, fields));
                assocDeletes.addAll(createAssocDelete(hldField, field, ctx, fields, fieldPK));
            }
            LLD.LLFieldValue fieldValue = new LLD.LLFieldValue(field, hldField.dvalue);
            fieldValue.dvalList = hldField.dvalueList;
            lldFields.add(fieldValue);
        }
        //Note. we generate an LLD statement even if llFields is empty (needed for MEM)
        return lldFields;
    }

    protected LLD.LLFieldValue createSingleField(LLD.LLTable llTable, TypePair pair, DValue dval) {
        LLD.LLField field = new LLD.LLField(pair, llTable, new LLD.DefaultLLNameFormatter());

        LLD.LLFieldValue fieldValue = new LLD.LLFieldValue(field, dval);
        fieldValue.dvalList = null; //TODO: is this OK
        return fieldValue;
    }

    protected List<LLD.LLInsert> createAssocInsert(HLD.HLDFieldValue hldField, LLD.LLField field, LLDBuilderContext ctx, List<HLD.HLDFieldValue> fields) {
        if (hldField.dvalue != null) {
            LLD.LLInsert ins = doCreateAssocInsert(hldField, hldField.dvalue, field, ctx, fields);
            return Collections.singletonList(ins);
        } else {
            List<LLD.LLInsert> list = new ArrayList<>();
            if (hldField.dvalueList != null) {
                for (DValue dval : hldField.dvalueList) {
                    LLD.LLInsert ins = doCreateAssocInsert(hldField, dval, field, ctx, fields);
                    list.add(ins);
                }
            }
            return list;
        }
    }

    protected List<LLD.LLDelete> createAssocDelete(HLD.HLDFieldValue hldField, LLD.LLField field, LLDBuilderContext ctx, List<HLD.HLDFieldValue> fields, HLD.HLDFieldValue fieldPK) {
        //if there are no inserts for a MM field then we need a delete to remove existing rows from DAT table
        if (hldField.dvalue == null && CollectionUtils.isEmpty(hldField.dvalueList) && fieldPK != null) {
            LLD.LLDelete del = doCreateAssocDelete(field, fieldPK, ctx);
            return Collections.singletonList(del);
        } else {
            return Collections.emptyList();
        }
    }

    protected LLD.LLInsert doCreateAssocInsert(HLD.HLDFieldValue hldField, DValue dval, LLD.LLField field, LLDBuilderContext ctx, List<HLD.HLDFieldValue> fields) {
        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(field.physicalTable.physicalType, field.physicalPair.name);
        AssocSpec assocSpec = datSvc.findAssocInfo(relinfo);
        DStructType assocType = LLDUtils.createDATType(ctx.registry, assocSpec);

        LLD.LLTable llTable = new LLD.LLTable(assocType, assocType, new LLD.DefaultLLNameFormatter());
        LLD.LLInsert insertLL = new LLD.LLInsert(ctx.loc);
        insertLL.table = llTable;
        insertLL.assocUpdateRelinfo = hldField.hldField.relinfo;

        boolean isNotFlipped = (assocSpec.leftType == field.physicalPair.type);
        if (isNotFlipped) {
            LLD.LLFieldValue fieldValue = createXField("leftv", field.crudAction, dval, llTable, assocSpec);
            insertLL.fieldL.add(fieldValue);

            DValue pkval = findPKVal(fields);
            fieldValue = createXField("rightv", field.crudAction, pkval, llTable, assocSpec);
            insertLL.fieldL.add(fieldValue);
        } else {
            LLD.LLFieldValue fieldValue = createXField("rightv", field.crudAction, dval, llTable, assocSpec);
            insertLL.fieldL.add(fieldValue);

            DValue pkval = findPKVal(fields);
            fieldValue = createXField("leftv", field.crudAction, pkval, llTable, assocSpec);
            insertLL.fieldL.add(fieldValue);
        }
        return insertLL;
    }

    protected LLD.LLDelete doCreateAssocDelete(LLD.LLField field, HLD.HLDFieldValue fieldPK, LLDBuilderContext ctx) {
        return doCreateAssocDeleteEx(field.physicalTable.physicalType, field.physicalPair, QueryType.PRIMARY_KEY, fieldPK.dvalue, null, ctx);
    }

    protected LLD.LLDelete doCreateAssocDeleteEx(DStructType structType, TypePair pair, QueryType queryType, DValue fkvalue,
                                                 Tok.WhereTok whereTok, LLDBuilderContext ctx) {
        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair.name);
        AssocSpec assocSpec = datSvc.findAssocInfo(relinfo);
        DStructType assocType = LLDUtils.createDATType(ctx.registry, assocSpec);

        LLD.LLTable llTable = new LLD.LLTable(assocType, assocType, new LLD.DefaultLLNameFormatter());
        LLD.LLDelete deleteLL = new LLD.LLDelete(ctx.loc);
        deleteLL.table = llTable;
        if (queryType.equals(QueryType.PRIMARY_KEY)) {
            boolean isNotFlipped = (assocSpec.leftType == pair.type);
            if (isNotFlipped) {
                //TODO fieldPK only should be used if updsert[55]
                deleteLL.whereTok = TokWhereClauseUtils.buildEqWhereClause(assocSpec.rightColumn, fkvalue, null);
            } else {
                deleteLL.whereTok = TokWhereClauseUtils.buildEqWhereClause(assocSpec.leftColumn, fkvalue, null);
            }
        } else if (queryType.equals(QueryType.ALL_ROWS)) {
            deleteLL.whereTok = TokWhereClauseUtils.buildTrueWhereClause(ctx.valueBuilder);
        } else if (QueryType.OP.equals(queryType)) {
            QueryTypeDetector qtd = new QueryTypeDetector();
            InQueryTypeDetails details = new InQueryTypeDetails();
            boolean supported = false;
            if (qtd.isInExpression(whereTok, details)) {
                //this is a special case of 'in' that we support in delete when ManyToMany
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
                if (details.allOp2AreValues && details.field.equals(pkpair.name)) {
                    supported = true;
                    boolean isNotFlipped = (assocSpec.leftType == pair.type);
                    if (isNotFlipped) {
                        deleteLL.whereTok = TokWhereClauseUtils.buildInWhereClause(assocSpec.rightColumn, details.inValues, null);
                    } else {
                        deleteLL.whereTok = TokWhereClauseUtils.buildInWhereClause(assocSpec.leftColumn, details.inValues, null);
                    }
                }
            }

            if (!supported) {
                DeliaExceptionHelper.throwNotImplementedError("delete %s - delete with join not yet supported", structType.getName());
            }
        }

        return deleteLL;
    }

    protected DValue findPKVal(List<HLD.HLDFieldValue> fields) {
        for (HLD.HLDFieldValue fieldv : fields) {
            PrimaryKey pk = fieldv.hldField.hldTable.getPrimaryKey();
            if (pk != null && pk.getFieldName().equals(fieldv.hldField.pair.name)) {
                return fieldv.dvalue;
            }

        }
        return null;
    }

    protected LLD.LLFieldValue createXField(String fieldName, CrudAction crudAction, DValue dval, LLD.LLTable llTable, AssocSpec assocSpec) {
        DStructType fieldStructType = assocSpec.getTypeForField(fieldName);
        PrimaryKey pk = fieldStructType.getPrimaryKey();

        TypePair pair = new TypePair(fieldName, pk.getKeyType());
        LLD.LLField field1 = new LLD.LLField(pair, llTable, new LLD.DefaultLLNameFormatter());
        field1.crudAction = crudAction; //propogate

        LLD.LLFieldValue fieldValue = new LLD.LLFieldValue(field1, dval);
//        fieldValue.dvalList = hldField.dvalueList;
        return fieldValue;
    }

}
