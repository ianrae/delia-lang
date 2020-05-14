package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.InsertContext;
import org.delia.db.schema.FieldInfo;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.StringTrail;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class AssocServiceTests extends NamedRelationTestBase {
	
	public interface ManyToManyVisitor {
		void visit(DStructType structType, RelationRuleBase rr);
	}

	public static class ManyToManyEnumerator {
		public void visitTypes(DTypeRegistry registry, ManyToManyVisitor visitor) {
			for(String typeName: registry.getAll()) {
				DType dtype = registry.getType(typeName);
				if (! dtype.isStructShape()) {
					continue;
				}
				DStructType structType = (DStructType) dtype;
				for(DRule rule: structType.getRawRules()) {
					if (rule instanceof RelationManyRule) {
						RelationManyRule rr = (RelationManyRule) rule;
						visitor.visit(structType, rr);
					}
				}
			}
		}
	}
	
	public static class MyManyToManyVisitor implements ManyToManyVisitor {
		public StringTrail trail = new StringTrail();
		
		@Override
		public void visit(DStructType structType, RelationRuleBase rr) {
			RelationInfo relinfo = rr.relInfo;
			String s = String.format("%s.%s", structType.getName(), relinfo.fieldName);
			trail.add(s);
		}
	}

	public static class PopulateDatIdVisitor implements ManyToManyVisitor {
		private FactoryService factorySvc;
		private DBInterface dbInterface;
		private DTypeRegistry registry;
		private SchemaMigrator schemaMigrator;
		private Log log;
		private Map<String,Integer> dataIdMap;

		public PopulateDatIdVisitor(FactoryService factorySvc, DBInterface dbInterface, DTypeRegistry registry, Log log) {
			this.factorySvc = factorySvc;
			this.dbInterface = dbInterface;
			this.registry = registry;
			this.log = log;
		}
		
		@Override
		public void visit(DStructType structType, RelationRuleBase rr) {
			if (rr.relInfo.getDatId() != null) {
				return;
			}
			
			loadSchemaFingerprintIfNeeded();
			String key = createKey(structType.getName(), rr.relInfo.fieldName);
			Integer datId = dataIdMap.get(key);
			if (datId != null) {  //will be null for new types
				rr.relInfo.forceDatId(datId);
				rr.relInfo.otherSide.forceDatId(datId);
			}
		}

		private void loadSchemaFingerprintIfNeeded() {
			//only read from DB if there are MM relations.
			if (schemaMigrator == null) {
				schemaMigrator = new SchemaMigrator(factorySvc, dbInterface, registry, new DoNothingVarEvaluator());
				String fingerprint = schemaMigrator.calcDBFingerprint();
				log.log("DB fingerprint: " + fingerprint);
				this.dataIdMap = buildDatIdMap(fingerprint);
			}
		}
		
		private Map<String,Integer> buildDatIdMap(String fingerprint) {
			Map<String,Integer> datMap = new HashMap<>();
			List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
			for(SchemaType sctype: list) {
				List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
				for(FieldInfo ff: fieldInfoL) {
					DType dtype = registry.getType(ff.type);
					if (dtype != null && dtype.isStructShape()) {
						String key = createKey(sctype.typeName, ff.name);
						int datId = ff.datId;
						datMap.put(key, datId);
						log.log(String.format("f %s %s", key, ff.type));
					}
				}
			}
			return datMap;
		}
		private String createKey(String typeName, String fieldName) {
			String key = String.format("%s.%s", typeName, fieldName);
			return key;
		}

		public SchemaMigrator getSchemaMigrator() {
			return schemaMigrator;
		}
	}

	public static class CreateNewDatIdVisitor implements ManyToManyVisitor {
		private FactoryService factorySvc;
		private DTypeRegistry registry;
		private SchemaMigrator schemaMigrator;
		private Log log;

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
	
	
	@Test
	public void test11() {
		createCustomer11TypeWithRelations("joe", null, "joe");
		RelationOneRule rr = getOneRule("Address", "cust");
		chkRule(rr, true, "joe", "joe");

		rr = getOneRule("Customer", "addr1");
		chkRule(rr, true, "joe", "joe");

		rr = getOneRule("Customer", "addr2");
		chkRuleOneSided(rr, false, "addr2");
		
		MyManyToManyVisitor visitor = new MyManyToManyVisitor();
		ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(sess.getExecutionContext().registry, visitor);
		assertEquals("", visitor.trail.getTrail());
	}

	@Test
	public void testMM() {
		createCustomerMMTypeWithRelations("joe", null, "joe");
		RelationManyRule rr = getManyRule("Address", "cust");
		chkRule(rr, true, "joe", "joe");

		MyManyToManyVisitor visitor = new MyManyToManyVisitor();
		ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(sess.getExecutionContext().registry, visitor);
		assertEquals("Address.cust;Customer.addr1", visitor.trail.getTrail());
		
		DTypeRegistry registry = sess.getExecutionContext().registry;
		SchemaMigrator schemaMigrator = new SchemaMigrator(delia.getFactoryService(), delia.getDBInterface(), registry, new DoNothingVarEvaluator());
//		schemaMigrator.dbNeedsMigration();
		String fingerprint = schemaMigrator.calcDBFingerprint();
		log(fingerprint);
		
		Map<String,Integer> datMap = new HashMap<>();
		List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
		for(SchemaType sctype: list) {
			List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
			for(FieldInfo ff: fieldInfoL) {
				DType dtype = registry.getType(ff.type);
				if (dtype != null && dtype.isStructShape()) {
					String key = String.format("%s.%s", sctype.typeName, ff.name);
					int datId = ff.datId;
					datMap.put(key, datId);
					log(String.format("f %s %s", key, ff.type));
				}
			}
		}
		
		schemaMigrator.close();
	}
	
	
	@Test
	public void testMM2() {
		createCustomerMMTypeWithRelations("joe", null, "joe");
		RelationManyRule rr = getManyRule("Address", "cust");
		chkRule(rr, true, "joe", "joe");

		DTypeRegistry registry = sess.getExecutionContext().registry;
		PopulateDatIdVisitor visitor = new PopulateDatIdVisitor(delia.getFactoryService(), delia.getDBInterface(), registry, delia.getLog());
		ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(sess.getExecutionContext().registry, visitor);
		
		CreateNewDatIdVisitor newIdVisitor = new CreateNewDatIdVisitor(delia.getFactoryService(), visitor.getSchemaMigrator(), registry, delia.getLog());
		enumerator = new ManyToManyEnumerator();
		enumerator.visitTypes(sess.getExecutionContext().registry, newIdVisitor);
		
		visitor.getSchemaMigrator().close();
	}

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}

	private String create11CustomerType(String relationName1, String relationName2, String relationName3) {
		String rn1 = relationName1 == null ? "" : String.format("'%s'", relationName1);
		String rn2 = relationName2 == null ? "" : String.format("'%s'", relationName2);
		String rn3 = relationName3 == null ? "" : String.format("'%s'", relationName3);

		String src = String.format("type Customer struct { id int primaryKey serial, wid int, relation addr1 Address %s one optional,", rn1);
		src += String.format("\n relation addr2 Address %s one optional} end", rn2);
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust Customer %s one} end",rn3);
		src += "\n";
		return src;
	}
	private String createMMCustomerType(String relationName1, String relationName2, String relationName3) {
		String rn1 = relationName1 == null ? "" : String.format("'%s'", relationName1);
		String rn2 = relationName2 == null ? "" : String.format("'%s'", relationName2);
		String rn3 = relationName3 == null ? "" : String.format("'%s'", relationName3);

		String src = String.format("type Customer struct { id int primaryKey serial, wid int, relation addr1 Address %s many optional,", rn1);
		src += String.format("\n relation addr2 Address %s one optional} end", rn2);
		src += "\n";
		src += String.format("\n type Address struct { id int primaryKey serial, z int, relation cust Customer %s many} end",rn3);
		src += "\n";
		return src;
	}
	

	private void createCustomer11TypeWithRelations(String relationName1, String relationName2, String relationName3) {
		String src = create11CustomerType(relationName1, relationName2, relationName3);
		log.log(src);
		execTypeStatement(src);
	}
	private void createCustomerMMTypeWithRelations(String relationName1, String relationName2, String relationName3) {
		String src = createMMCustomerType(relationName1, relationName2, relationName3);
		log.log(src);
		execTypeStatement(src);
	}

}
