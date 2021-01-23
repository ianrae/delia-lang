package org.delia.assoc;


import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.QueryBuilderService;
import org.delia.db.hld.HLDSimpleQueryService;
import org.delia.db.hld.SqlColumn;
import org.delia.db.hld.cud.HLDInsert;
import org.delia.db.hld.cud.TypeOrTable;
import org.delia.db.hld.simple.SimpleInsert;
import org.delia.db.hld.simple.SimpleSqlGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.zdb.ZDBExecutor;

public class CreateNewDatIdVisitor implements ManyToManyVisitor {
	public static boolean hackFlag = false; //for unit tests
	
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Log log;
	public int datIdCounter;
	private QueryBuilderService queryBuilder;
	private boolean haveInitTableNameCreator = false;
	private int nextAssocNameInt;
	private DatIdMap datIdMap;
	private ZDBExecutor dbexecutor;
	private boolean flippedByHackFlag; //for unit tests only

	public CreateNewDatIdVisitor(FactoryService factorySvc, ZDBExecutor zdbExecutor, DTypeRegistry registry, Log log, DatIdMap datIdMap) {
		this.factorySvc = factorySvc;
		this.dbexecutor = zdbExecutor;
		this.registry = registry;
		this.log = log;
		this.queryBuilder = factorySvc.getQueryBuilderService();
		this.datIdMap = datIdMap;
	}
	
	@Override
	public void visit(DStructType structType, RelationRuleBase rr) {
		if (rr.relInfo.getDatId() != null) {
			return;
		}
		
		initTableNameCreatorIfNeeded();
		
		//create new row 
		//write new schema to db
		DStructType dtype = registry.getDATType();
		String tblName = createAssocTableName(rr.relInfo);
		DValue dval = createDatTableObj(dtype, tblName, rr.relInfo);
		if (dval == null) {
			return;
		}

		DValue newDatIdValue = doRawInsert(dval, tblName); 
		
		if (newDatIdValue != null) {  
			int datId = newDatIdValue.asInt();
			rr.relInfo.forceDatId(datId);
			rr.relInfo.otherSide.forceDatId(datId);
			String key = createKey(structType.getName(), rr.relInfo.fieldName);
			String left = dval.asStruct().getField("leftName").asString();
			String right = dval.asStruct().getField("rightName").asString();
			log.log("DAT: %s -> datId: %d (table: %s) left:%s, right:%s", key, datId, tblName, left, right);
			datIdCounter++;
			datIdMap.putFull(key, datId, tblName, left, right);
		}
	}
	
	private DValue doRawInsert(DValue dval, String tblName) {
		InsertContext ictx = new InsertContext();
		ictx.extractGeneratedKeys = true;
		ictx.genKeytype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		
		DValue newDatIdValue = null;
		if (dbexecutor.getDbInterface().getDBType().equals(DBType.MEM)) {
			ictx.actualDValForRawInsert = dval;
			newDatIdValue = dbexecutor.rawInsert(null, ictx);
		} else {
			//must build
			SimpleSqlGenerator sqlgen = new SimpleSqlGenerator(registry, factorySvc);
			SimpleInsert simple = new SimpleInsert();
			simple.tblFrag = new SqlColumn(null, tblName);
			simple.hld = new HLDInsert(new TypeOrTable(tblName, true));
			for(String fieldName: dval.asMap().keySet()) {
				DValue inner = dval.asMap().get(fieldName);
				SqlColumn col = new SqlColumn(null, fieldName);
				simple.fieldL.add(col);
				
				simple.hld.valueL.add(inner);
			}
			
			SqlStatement stm = new SqlStatement(null);
			stm.sql = sqlgen.gen(simple, stm);
			newDatIdValue = dbexecutor.rawInsert(stm, ictx);
		}
		
		return newDatIdValue;
	}

	public void initTableNameCreatorIfNeeded() {
		if (haveInitTableNameCreator) {
			return; //only do this once
		}
		haveInitTableNameCreator = true;
		
		//DAT tables are named dat1,dat2, etc
		//we need to determine how many datIds there are so we
		//can generate new names. i.e. if there are 3 then we should
		//be generating dat4,dat5,...
		
		DStructType datType = registry.getDATType();
		QueryExp exp = queryBuilder.createAllRowsQuery(datType.getName());
//		HLSSimpleQueryService querySvc = factorySvc.createSimpleQueryService(dbexecutor.getDbInterface(), registry);
//		QueryResponse qresp = querySvc.execQuery(exp, dbexecutor);
		HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(dbexecutor.getDbInterface(), registry);
		QueryResponse qresp = querySvc.execQuery(exp, dbexecutor);
		
		int maxDatId = loadDATRows(qresp);
		log.log("DAT: max id %d.", maxDatId);
		nextAssocNameInt = maxDatId + 1; //start at one
	}
	
	int loadDATRows(QueryResponse qresp) {
		if (qresp.emptyResults()) {
			return 0;
		}
		
		int maxId = 0;
		for(DValue dval: qresp.dvalList) {
			int id = dval.asStruct().getField("id").asInt();
			if (id > maxId) {
				maxId = id;
			}
			
			String tblName = dval.asStruct().getField("tblName").asString();
			String left = dval.asStruct().getField(DatIdMapHelper.LEFTNAME).asString();
			String right = dval.asStruct().getField(DatIdMapHelper.RIGHTNAME).asString();
			datIdMap.attachTblName(id, tblName, left, right);
		}

		return maxId;
	}
	
	private String createAssocTableName(RelationInfo relInfo) {
		String s1 = relInfo.nearType.getName();
		String s2 = relInfo.farType.getName();
		
		if (hackFlag) {
			String tblName = String.format("%s%sDat%d", s1, s2, nextAssocNameInt);
			if (this.dbexecutor.doesTableExist(tblName)) {
				nextAssocNameInt++;
				this.flippedByHackFlag = false;
				return tblName;
			} else {
				tblName = String.format("%s%sDat%d", s2, s1, nextAssocNameInt);
				if (this.dbexecutor.doesTableExist(tblName)) {
					nextAssocNameInt++;
					this.flippedByHackFlag = true;
					return tblName;
				}
			}
		}
		
		String tlbName = String.format("%s%sDat%d", s1, s2, nextAssocNameInt++);
		return tlbName;
	}

	private DValue createDatTableObj(DStructType type, String datTableName, RelationInfo relInfo) {
		StructValueBuilder structBuilder = new StructValueBuilder(type);

		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildString(datTableName);
		structBuilder.addField("tblName", dval);
		DValue left = buildLRString(builder, relInfo);
		DValue right = buildLRString(builder, relInfo.otherSide);
		if (flippedByHackFlag) {
			DValue tmp = left;
			left = right;
			right = tmp;
		}
		
		structBuilder.addField(DatIdMapHelper.LEFTNAME, left);
		structBuilder.addField(DatIdMapHelper.RIGHTNAME, right);
		//TODO: schema migrator needs to track rename-field and update DAT table too

		boolean b = structBuilder.finish();
		if (! b) {
			log.logError("failed to build DAT row for type '%s'", type.getName());
			return null;
		}
		dval = structBuilder.getDValue();
		return dval;
	}

	private DValue buildLRString(ScalarValueBuilder builder, RelationInfo relInfo) {
		String left = String.format("%s.%s", relInfo.nearType, relInfo.fieldName);
		return builder.buildString(left);
	}

	private String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}
	
}