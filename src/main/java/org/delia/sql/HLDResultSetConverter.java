package org.delia.sql;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.db.DBException;
import org.delia.db.ValueHelper;
import org.delia.dval.DRelationHelper;
import org.delia.error.DeliaError;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.StructValueBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ian Rae
 * <p>
 * A newer algorithm using HLS and Rendered fields. We read fields by column index in the order specified in SQL statement.
 */
public class HLDResultSetConverter extends HLDResultSetConverterBase {

    private AssocPoolMerger assocPoolMerger;
    private DatService datSvc;

    static class ColumnRun {
        public DStructType dtype;
        public List<LLD.LLField> runList = new ArrayList<>();
        public int iStart;
        public String columnKey; //the alias
        public Exp.JoinInfo joinInfo;

        public ColumnRun(int i, DStructType dtype, String columnKey) {
            this.iStart = i;
            this.dtype = dtype;
            this.columnKey = columnKey;
        }
    }

    static class AssocColInfo {
        public DValue parentVal; //struct value in which dval is a field
        public ColumnRun columnRun;
        public DValue dval;

        public AssocColInfo(DValue parentVal, ColumnRun columnRun, DValue dval) {
            this.parentVal = parentVal;
            this.columnRun = columnRun;
            this.dval = dval;
        }
    }

    private DTypeRegistry registry;

    public HLDResultSetConverter(FactoryService factorySvc, ValueHelper valueHelper, DTypeRegistry registry, DatService datSvc) {
        super(factorySvc, valueHelper);
        this.registry = registry;
        this.datSvc = datSvc;
        this.assocPoolMerger = new AssocPoolMerger(factorySvc, registry, datSvc);
        this.compareSvc = factorySvc.getDValueCompareService();
    }


    public List<DValue> buildDValueList(ResultSet rs, DBAccessContext dbctx, LLD.LLSelect stmt) {
//		if (hls == null) { //TODO: or if is select *
//			return super.buildDValueList(rs, dtype, details, dbctx, hls);
//		} else if (!hls.hlspanL.isEmpty() && !hls.hlspanL.get(0).renderedFieldL.isEmpty()) {
//			boolean isStarQuery = hls.hlspanL.get(0).renderedFieldL.get(0).field.equals("*");
//			if (isStarQuery) {
//				return super.buildDValueList(rs, dtype, details, dbctx, hls);
//			}
//		}

        ResultSetWrapper rsw = new ResultSetWrapper(rs, valueHelper, logResultSetDetails, log);
        List<DValue> list = null;
        ObjectPool pool = new ObjectPool(factorySvc, registry);
        try {
            list = doBuildDValueList(rsw, dbctx, stmt, pool);
        } catch (ValueException e) {
            ValueException ve = (ValueException) e;
            throw new DBException(ve.errL);
        } catch (Exception e) {
            e.printStackTrace();
            DeliaError err = new DeliaError("db-resultset-error", e.getMessage());
            throw new DBException(err);
        }

        return list;
    }


    private List<DValue> doBuildDValueList(ResultSetWrapper rsw, DBAccessContext dbctx, LLD.LLSelect stmt, ObjectPool pool) throws Exception {
        List<DValue> list = new ArrayList<>();

        List<LLD.LLField> rfList = new ArrayList<>();
        for (LLD.LLEx fld : stmt.fields) {
            LLD.LLField field = (LLD.LLField) fld;
            rfList.add(field);
        }
        HLDFieldHelper.logRenderedFieldList(stmt, rfList, log);

        //add column indexes
        int j = 1;
        for (LLD.LLField rff : rfList) {
            rff.columnIndex = j++;
        }

        List<ColumnRun> columnRunL = buildColumnRuns(stmt, rfList);
        HLDFieldHelper.logRenderedColumnRuns(stmt, columnRunL, log);
//        List<AssocColInfo> assocPool = new ArrayList<>();

        while (rsw.next()) {  //get row
            //do main type
            ColumnRun mainRun = columnRunL.get(0);
            DValue dval = readStructDValueX(mainRun, rsw, dbctx);
            if (dval == null) {
                DeliaExceptionHelper.throwError("unexpected-null-db-results", "%s: unexpected null dval", stmt.getTableName());
            }
            list.add(dval);

            //do remaining column runs
            for (int i = 1; i < columnRunL.size(); i++) {
                ColumnRun columnRun = columnRunL.get(i);
                DValue subDVal = readStructDValueX(columnRun, rsw, dbctx);
                if (subDVal != null) {
                    if (isAssocObj(columnRun)) {
                        //note. we are adding repeats (eg. several copies of cust:100)
//                        assocPool.add(new AssocColInfo(dval, columnRun, subDVal));
                        LLD.LLField fff = columnRun.runList.get(0);
                        AssocColInfo aci = new AssocColInfo(dval, columnRun, subDVal);
                        TypePair pp = new TypePair(fff.joinInfo.throughField, fff.joinInfo.rightType);
                        this.assocPoolMerger.xmergeAssocItems(pp, fff.joinInfo, dval, aci, stmt, dbctx);
                    } else if (isMoreThanAnFK(dval, subDVal, columnRun, dbctx)) {
                        String relationName = getRelationNameIfAny(columnRun, stmt);
                        pool.add(subDVal, relationName);
                    }
                }
            }
            pool.add(dval, null);
        }

        log.log("rs had %d rows", list.size());
        list = mergeRows(list, pool, columnRunL, stmt, dbctx);
        return list;
    }

    //if there are more than one relations of same type in a struct, relationName must be used
    private String getRelationNameIfAny(ColumnRun columnRun, LLD.LLSelect stmt) {
        for (LLD.LLEx ff : stmt.fields) {
            if (ff instanceof LLD.LLField) {
                LLD.LLField field = (LLD.LLField) ff;
                if (field.joinInfo != null) {
                    if (field.joinInfo.relinfo.relationName != null) {
                        return field.joinInfo.relinfo.relationName;
                    }
                }
            }
        }
        return null;
    }

    private boolean isAssocObj(ColumnRun columnRun) {
        for (LLD.LLField fld : columnRun.runList) {
            if (fld.isAssocField) {
                return true;
            }
        }
        return false;
    }

    private List<ColumnRun> buildColumnRuns(LLD.LLSelect stmt, List<LLD.LLField> rfList) {
        DStructType dtype = stmt.table.physicalType; //.hldquery.fromType;

        List<ColumnRun> resultL = new ArrayList<>();
        String currentKey = stmt.table.alias; //TODO: sometimes this may be wrong?
        ColumnRun run = new ColumnRun(0, dtype, currentKey);
        resultL.add(run);

        DStructType currentType = dtype;
        int iEnd = 0;
        for (int i = 0; i < rfList.size(); i++) {
            LLD.LLField rff = rfList.get(i);

            DStructType currentStructType = getFieldStructType(rff, currentType);
            String tmpKey = rff.joinInfo == null ? rff.physicalTable.alias : rff.joinInfo.alias;
            if (tmpKey.equals(currentKey)) {
                iEnd = i;
            } else {
                copyToRunList(run, iEnd, rfList);

                run = new ColumnRun(i, currentStructType, tmpKey);
                run.joinInfo = rff.joinInfo;
                resultL.add(run);
                currentType = currentStructType;
                currentKey = tmpKey;
                iEnd = i;
            }
        }
        copyToRunList(run, iEnd, rfList);

        return resultL;
    }

    private void copyToRunList(ColumnRun run, int iEnd, List<LLD.LLField> rfList) {
        List<LLD.LLField> tmpL = rfList.subList(run.iStart, iEnd + 1);
        run.runList.addAll(tmpL);
    }

    private DStructType getFieldStructType(LLD.LLField rff, DStructType currentType) {
        if (rff.physicalPair.type != null) {
            return rff.physicalTable.physicalType;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("wtf!");
            return null;
        }
    }

    private DValue readStructDValueX(ColumnRun columnRun, ResultSetWrapper rsw, DBAccessContext dbctx) throws Exception {
        DStructType dtype = columnRun.dtype;
        StructValueBuilder structBuilder = new StructValueBuilder(dtype);
        structBuilder.setIgnoreMissingFields(true); //we validate at a higher level. down here we don't know how many fields are being read
        PrimaryKey pk = dtype.getPrimaryKey();

        for (LLD.LLField rff : columnRun.runList) {
            TypePair pair = rff.physicalPair;
            DValue inner = null;
            if (rff.isAssocField) {
//                structBuilder.setIgnoreMissingFields(true);
                inner = doAssocField(rsw, rff, dbctx);
                if (inner != null) {
                    //In the DB assoc fields are mandatory, but they are defined as optional in a type like CustomerAddressDat1
                    structBuilder.addField(pair.name, inner);
                }
            } else {
                inner = rsw.readFieldByColumnIndex(rff.physicalPair, rff.columnIndex, dbctx);
                if (inner == null && pk != null && pair.name.equals(pk.getFieldName())) {
                    //is optional relation and is null
                    return null;
                }

                //handle relation. inner is the pkval
                boolean isStruct = pair.type.isStructShape();
                if (inner != null && isStruct) {
                    DValue pkval = inner;
                    if (dtype.equals(rff.physicalTable.physicalType)) {
                        inner = this.createEmptyRelation(dbctx, rff.physicalTable.physicalType, pair.name);
                    } else {
                        inner = this.createEmptyRelation(dbctx, (DStructType) rff.physicalPair.type, pair.name);
                    }
                    DRelation drel = inner.asRelation();
                    drel.addKey(pkval);
                }

                String fieldName = pair.name;
//                if (pk!pair.name.equals(pk.getFieldName()) && isStruct) {
                if (isStruct) {
                    fieldName = pair.name;
//                    fieldName = rff.joinInfo != null ? buildJoinFieldName(rff, dtype) : pair.name;
                }
                structBuilder.addField(fieldName, inner);
            }
        }

        boolean b = structBuilder.finish();
        if (!b) {
            //JTElement el = columnRun.getJTElementIfExist();
            boolean needAllColumns = !areAnyJoinsFKJoins(columnRun);
            //if we're doing .fks() then are only getting pk, not all the columns
            //TODO: only ignore missing field errors. other types of validation errors should still be thrown!
            if (needAllColumns) {
                throw new ValueException(structBuilder.getValidationErrors());
            }
        }
        DValue dval = structBuilder.getDValue();
        return dval;
    }

    private String buildJoinFieldName(LLD.LLField rff, DStructType dtype) {
        RelationInfo relinfo = rff.joinInfo.relinfo;
        if (rff.joinInfo.isSelfJoin()) {
            return relinfo.fieldName;
        } else if (dtype == relinfo.nearType) {
            return relinfo.fieldName;
        } else {
            return relinfo.otherSide.fieldName; //TODO what about one-sided relations?
        }
    }

    private DValue doAssocField(ResultSetWrapper rsw, LLD.LLField rff, DBAccessContext dbctx) throws SQLException {
        AssocSpec assocSpec = datSvc.findByAssocTableName(rff.getTableName());
        DStructType fieldStructType = assocSpec.getTypeForField(rff.getFieldName());
        PrimaryKey pk = fieldStructType.getPrimaryKey();
        TypePair pair = new TypePair(rff.getFieldName(), pk.getKeyType());

        DValue inner = rsw.readFieldByColumnIndex(pair, rff.columnIndex, dbctx);
        //can be null since MM relations can be optional (relation addr Address optional many)
        return inner;
    }


    private boolean areAnyJoinsFKJoins(ColumnRun columnRun) {
//		for(HLDField field: columnRun.runList) {
//			if (field.source instanceof JoinElement) {
//				JoinElement jel = (JoinElement) field.source;
//				if (jel.usedForFK()) {
//					return true;
//				}
//			}
//		}
        return false;
    }

    private boolean isMoreThanAnFK(DValue dval, DValue subDVal, ColumnRun columnRun, DBAccessContext dbctx) {
//        if (columnRun.runList.size() != 1) {
//            return true;
//        }
        LLD.LLField field = columnRun.runList.get(0);
        if (field.joinInfo != null && (field.joinInfo.isFKOnly || field.joinInfo.isFetch)) {

            //add subDVal's field value as FK here. Don't need to add to pool.
            String relField = field.joinInfo.throughField; //TODO: what if flipped?
            DValue inner = createEmptyRelation(dbctx, (DStructType) dval.getType(), relField);
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.physicalTable.physicalType);
            DValue fkval = subDVal.asStruct().getField(pkpair.name);
            DRelation drel = inner.asRelation();
            drel.addKey(fkval);
            dval.asMap().put(relField, inner);
            return field.joinInfo.isFetch;
        }

        return true;
    }

    /**
     * On a Many-to-many relation our query returns multiple rows in order to get all
     * the 'many' ids.
     *
     * @param rawList    list of dvalues to merge
     * @param assocPool
     * @param columnRunL
     * @param hld
     * @param dbctx
     * @return merged rows
     */
    private List<DValue> mergeRows(List<DValue> rawList, ObjectPool pool, List<ColumnRun> columnRunL, LLD.LLSelect hld, DBAccessContext dbctx) {
        //build output list. keep same order
        List<DValue> resultList = new ArrayList<>();
        boolean usePoolInMerge = true; //usePoolInMerge(columnRunL, hld);

//		Map<DRelation,String> alreadyMap = new HashMap<>();
        for (DValue dval : rawList) {
            //don't always need this. pool has already removed duplicates
            if (usePoolInMerge) {
                if (!pool.contains(dval)) {
                    continue;
                }
            }
            resultList.add(dval);

            fillInAssocOrFetchedItems(dval, pool, columnRunL, hld, dbctx);
            sortFKsIfNeeded(dval);
        }

        return resultList;
    }

    private void fillInAssocOrFetchedItems(DValue dval, ObjectPool pool, List<ColumnRun> columnRunL, LLD.LLSelect hld, DBAccessContext dbctx) {
        DStructType dtype = (DStructType) dval.getType();
        for (TypePair pair : dtype.getAllFields()) {
            if (pair.type.isStructShape()) {
                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(dtype, pair);
                DValue inner = dval.asStruct().getField(pair.name);
                if (inner == null) {
                    if (relinfo.isManyToMany()) {
//                        assocPoolMerger.mergeAssocItems(pair, dval, assocPool, hld, dbctx);
                    } else {
//                        inner = this.createEmptyRelation(dbctx, dtype, pair.name);
                        //DValue foreignVal = pool.findMatch(pair.type, pkval, relinfo.relationName);

                    }
                } else {
                    DRelation drel = inner.asRelation();
                    //TODO: do we need to sort fetched items
//                    if (drel.haveFetched()) {
//                        //TODO: this is expensive. should probably only be turned on in unit tests
//                        //hmm. this was leading to a list of [55,56,57] becoming just [56].
////						for(DValue fetchedVal: drel.getFetchedItems()) {
////							sortFKsIfNeeded(fetchedVal);
////						}
//                    }
//
                    List<DValue> fkList = new ArrayList<>(drel.getMultipleKeys()); //avoid concurrent modification exception
                    for (DValue pkval : fkList) {
                        DValue foreignVal = pool.findMatch(pair.type, pkval, relinfo.relationName);
                        if (foreignVal != null) { //can be null if only doing fks()
                            DValue existing = DRelationHelper.findInFetchedItems(drel, pkval);
                            if (existing == null) {
                                DRelationHelper.addToFetchedItems(drel, foreignVal);
                            } else {
//								//TODO fix. for each object (eg Address.100) there should only be one instance
//								//but somehow there is two
//								//In this else we add the 2nd instance which results in #fetched > #fks
//								//But seede figures and removes the wrong one.
//								DRelationHelper.addToFetchedItems(drel, foreignVal);
                            }

                            //Address.cust should point back to dval
                            addReverseFK(dval, foreignVal, relinfo, dbctx);


//                            if (doInner) {
//                                fillInAssocOrFetchedItems(foreignVal, pool, false, columnRunL, hld, dbctx); //** recursion **
//                            }
                        }
                    }

                }
            }
        }
    }

    private void addReverseFK(DValue dval, DValue foreignVal, RelationInfo relinfo, DBAccessContext dbctx) {
        DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
        String reverseField = relinfo.otherSide.fieldName; //cust
        DValue reverseInner = foreignVal.asStruct().getField(reverseField);
        if (reverseInner == null) {
            reverseInner = createEmptyRelation(dbctx, (DStructType) foreignVal.getType(), reverseField);
            DRelation drel = reverseInner.asRelation();
            drel.addKey(pkval);
            foreignVal.asMap().put(reverseField, reverseInner);
//        } else if (!reverseInner.getType().isStructShape()) {
//            //TODO: WTF is this???
//            DValue tmp = createEmptyRelation(dbctx, relinfo.farType, reverseField);
//            DRelation drel = tmp.asRelation();
//            drel.addKey(pkval);
//            foreignVal.asMap().put(reverseField, reverseInner);
        } else {
            DRelationHelper.addIfNotExist(reverseInner.asRelation(), pkval, compareSvc);
        }
    }

    private void sortFKsIfNeeded(DValue dval) {
        DStructType dtype = (DStructType) dval.getType();
        for (TypePair pair : dtype.getAllFields()) {
            if (pair.type.isStructShape()) {
                DValue inner = dval.asStruct().getField(pair.name);
                if (inner != null) {
                    DRelation drel = inner.asRelation();
                    DRelationHelper.sortFKs(drel); //this is not needed, but simplifies unit tests
                }
            }
        }
    }

}