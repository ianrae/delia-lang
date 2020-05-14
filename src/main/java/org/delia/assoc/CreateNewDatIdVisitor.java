package org.delia.assoc;


import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.DBExecutor;
import org.delia.db.InsertContext;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.schema.SchemaMigrator;
import org.delia.log.Log;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

public class CreateNewDatIdVisitor implements ManyToManyVisitor {
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Log log;
	public int datIdCounter;
	private QueryBuilderService queryBuilder;
	private boolean haveInitTableNameCreator = false;
	private int nextAssocNameInt;
	private DatIdMap datIdMap;
	private DBExecutor dbexecutor;

	public CreateNewDatIdVisitor(FactoryService factorySvc, DBExecutor dbexecutor, DTypeRegistry registry, Log log, DatIdMap datIdMap) {
		this.factorySvc = factorySvc;
		this.dbexecutor = dbexecutor;
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
		String tblName = createAssocTableName();
		DValue dval = createDatTableObj(dtype, tblName);
		if (dval == null) {
			return;
		}

		InsertContext ictx = new InsertContext();
		ictx.extractGeneratedKeys = true;
		ictx.genKeytype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		DValue newDatIdValue = dbexecutor.executeInsert(dval, ictx);
		
		if (newDatIdValue != null) {  
			int datId = newDatIdValue.asInt();
			rr.relInfo.forceDatId(datId);
			rr.relInfo.otherSide.forceDatId(datId);
			String key = createKey(structType.getName(), rr.relInfo.fieldName);
			log.log("DAT: %s -> datId: %d (table: %s)", key, datId, tblName);
			datIdCounter++;
			datIdMap.putFull(key, datId, tblName);
		}
	}
	
	public void initTableNameCreatorIfNeeded() {
		if (haveInitTableNameCreator) {
			return; //only do this once
		}
		haveInitTableNameCreator = true;
		
		//DAT tables are named dat1,dat2, etc
		//we need to determine how many datIds there are so we
		//can generate new names. i.e. if there are 3 then we should
		//generate dat4,dat5,...
		//TODO: we could instead calc highest-datid during first visitor
		
		DStructType datType = registry.getDATType();
		QueryExp exp = queryBuilder.createAllRowsQuery(datType.getName());
		QuerySpec spec = queryBuilder.buildSpec(exp, new DoNothingVarEvaluator());
		QueryResponse qresp = dbexecutor.executeQuery(spec, new QueryContext());
		
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
			datIdMap.attachTblName(id, tblName);
		}

		return maxId;
	}
	
	private String createAssocTableName() {
		String tlbName = String.format("dat%d", nextAssocNameInt++);
		return tlbName;
	}

	private DValue createDatTableObj(DStructType type, String datTableName) {
		StructValueBuilder structBuilder = new StructValueBuilder(type);

		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildString(datTableName);
		structBuilder.addField("tblName", dval);

		boolean b = structBuilder.finish();
		if (! b) {
			return null;
		}
		dval = structBuilder.getDValue();
		return dval;
	}

	private String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}
	
}