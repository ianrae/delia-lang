package org.delia.assoc;

import org.delia.core.FactoryService;
import org.delia.db.InsertContext;
import org.delia.db.schema.SchemaMigrator;
import org.delia.log.Log;
import org.delia.rule.rules.RelationRuleBase;
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

	public CreateNewDatIdVisitor(FactoryService factorySvc, SchemaMigrator schemaMigrator, DTypeRegistry registry, Log log) {
		this.factorySvc = factorySvc;
		this.schemaMigrator = schemaMigrator;
		this.registry = registry;
		this.log = log;
	}
	
	@Override
	public void visit(DStructType structType, RelationRuleBase rr) {
		if (rr.relInfo.getDatId() != null) {
			return;
		}
		
		//create new row 
		//write new schema to db
		DStructType dtype = registry.getDATType();
		DValue dval = createDatTableObj(dtype, "dat11");
		if (dval == null) {
			return;
		}

		InsertContext ictx = new InsertContext();
		DValue newDatIdValue = schemaMigrator.getDbexecutor().executeInsert(dval, ictx);
		
		if (newDatIdValue != null) {  
			rr.relInfo.forceDatId(newDatIdValue.asInt());
			rr.relInfo.otherSide.forceDatId(newDatIdValue.asInt());
			String key = createKey(structType.getName(), rr.relInfo.fieldName);
			log.log("key: %s, created datId: %d", key, newDatIdValue.asInt());
			datIdCounter++;
		}
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