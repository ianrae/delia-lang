package org.delia.relation.named;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.assoc.DatIdMap;
import org.delia.assoc.ManyToManyEnumerator;
import org.delia.assoc.ManyToManyVisitor;
import org.delia.assoc.PopulateDatIdVisitor;
import org.delia.db.schema.DatMapBuilder;
import org.delia.db.schema.FieldInfo;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDFactoryImpl;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.RelationRuleBase;
import org.delia.runner.DeliaException;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringTrail;
import org.delia.zdb.DBExecutor;
import org.junit.Before;
import org.junit.Test;

public class AssocServiceTests extends NamedRelationTestBase {
	
	public static class MyManyToManyVisitor implements ManyToManyVisitor {
		public StringTrail trail = new StringTrail();
		
		@Override
		public void visit(DStructType structType, RelationRuleBase rr) {
			RelationInfo relinfo = rr.relInfo;
			String s = String.format("%s.%s", structType.getName(), relinfo.fieldName);
			trail.add(s);
		}
	}

	@Test(expected=DeliaException.class)
	public void test11() {
		// type-dependency-cycle
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
		assertEquals("Customer.addr1;Address.cust", visitor.trail.getTrail());
		
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DatIdMap datIdMap = null; //TODO is this ok?
		SchemaMigrator schemaMigrator = new SchemaMigrator(delia.getFactoryService(), delia.getDBInterface(), registry, new DoNothingVarEvaluator(), datIdMap);
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
		//clear datIds
		rr.relInfo.forceDatId(null);;
		rr = getManyRule("Customer", "addr1");
		rr.relInfo.forceDatId(null);;
		
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DatIdMap datIdMap = null; //TODO is this ok?

		try(SchemaMigrator migrator = new SchemaMigrator(factorySvc, dbInterface, registry, new DoNothingVarEvaluator(), datIdMap)) {
			DatMapBuilder datMapBuilder = migrator.createDatMapBuilder();
			PopulateDatIdVisitor visitor = new PopulateDatIdVisitor(datMapBuilder, registry, delia.getLog());
			ManyToManyEnumerator enumerator = new ManyToManyEnumerator();
			enumerator.visitTypes(sess.getExecutionContext().registry, visitor);
			datIdMap = visitor.getDatIdMap();

			DBExecutor rawExecutor = migrator.getZDBExecutor();
			CreateNewDatIdVisitor newIdVisitor = new CreateNewDatIdVisitor(delia.getFactoryService(), rawExecutor, registry, delia.getLog(), datIdMap);
			enumerator = new ManyToManyEnumerator();
			enumerator.visitTypes(sess.getExecutionContext().registry, newIdVisitor);
			
			migrator.close();
			
		}
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
