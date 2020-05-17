//package org.delia.db.memdb;
//
//import org.delia.core.DateFormatService;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.DBAccessContext;
//import org.delia.db.DBException;
//import org.delia.db.InsertContext;
//import org.delia.db.memdb.MemDBInterface.Stuff;
//import org.delia.error.DeliaError;
//import org.delia.type.DStructType;
//import org.delia.type.DValue;
//import org.delia.type.TypePair;
//
///**
// * Performs update
// * 
// * @author Ian Rae
// *
// */
//public class MemInsert extends ServiceBase {
//
//	DateFormatService fmtSvc;
//
//	public MemInsert(FactoryService factorySvc) {
//		super(factorySvc);
//		this.fmtSvc = factorySvc.getDateFormatService();
//	}
//
//	public DValue doExecuteInsert(MemDBTable tbl, DValue dval, InsertContext ctx, DBAccessContext dbctx, MemDBInterface memDBInterface, Stuff stuff) {
//		String typeName = dval.getType().getName();
//
//		DValue generatedId = addSerialValuesIfNeeded(dval, tbl, dbctx, stuff);
//		memDBInterface.checkUniqueness(dval, tbl, typeName, null, false, dbctx);
//
//		tbl.rowL.add(dval);
//		return generatedId;
//	}
//
//	private DValue addSerialValuesIfNeeded(DValue dval, MemDBTable tbl, DBAccessContext dbctx, Stuff stuff) {
//		if (!dval.getType().isStructShape()) {
//			return null;
//		}
//		DValue generatedId = null;
//		DStructType structType = (DStructType) dval.getType();
//		for(TypePair pair: structType.getAllFields()) {
//			if (structType.fieldIsSerial(pair.name)) {
//				if (dval.asStruct().getField(pair.name) != null) {
//					DeliaError err = et.add("serial-value-cannot-be-provided", String.format("serial field '%s' must not have a value specified", pair.name));
//					throw new DBException(err);
//				}
//
//				DValue serialVal = stuff.serialProvider.generateSerialValue(structType, pair);
//				dval.asMap().put(pair.name, serialVal);
//				generatedId = serialVal;
//				log.logDebug("serial id generated: %s", serialVal.asString());
//			}
//		}
//		return generatedId;
//	}
//	
//}