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
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

public class CreateNewDatIdVisitor implements ManyToManyVisitor {
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private SchemaMigrator schemaMigrator;
	private Log log;
	public int datIdCounter;
	private QueryBuilderService queryBuilder;
	private int nextAssocNameInt;

	public CreateNewDatIdVisitor(FactoryService factorySvc, SchemaMigrator schemaMigrator, DTypeRegistry registry, Log log) {
		this.factorySvc = factorySvc;
		this.schemaMigrator = schemaMigrator;
		this.registry = registry;
		this.log = log;
		this.queryBuilder = factorySvc.getQueryBuilderService();
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
		DValue newDatIdValue = schemaMigrator.getDbexecutor().executeInsert(dval, ictx);
		
		if (newDatIdValue != null) {  
			rr.relInfo.forceDatId(newDatIdValue.asInt());
			rr.relInfo.otherSide.forceDatId(newDatIdValue.asInt());
			String key = createKey(structType.getName(), rr.relInfo.fieldName);
			log.log("DAT: %s -> datId: %d (table: %s)", key, newDatIdValue.asInt(), tblName);
			datIdCounter++;
		}
	}
	
	private void initTableNameCreatorIfNeeded() {
		DStructType datType = registry.getDATType();
		QueryExp exp = queryBuilder.createCountQuery(datType.getName());
		QuerySpec spec = queryBuilder.buildSpec(exp, new DoNothingVarEvaluator());
		DBExecutor dbexecutor = schemaMigrator.getDbexecutor();
		QueryResponse qresp = dbexecutor.executeQuery(spec, new QueryContext());
		int numAssocTbls = qresp.emptyResults() ? 0 : qresp.getOne().asInt();
		nextAssocNameInt = numAssocTbls + 1; //start at one
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