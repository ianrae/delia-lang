package org.delia.schema;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.DBType;
import org.delia.db.schema.modify.SchemaChangeOperation;
import org.delia.db.schema.modify.SchemaDefinition;
import org.delia.db.schema.modify.SchemaDefinitionGenerator;
import org.delia.db.schema.modify.SchemaDelta;
import org.delia.db.schema.modify.SchemaDeltaGenerator;
import org.delia.db.schema.modify.SchemaDeltaOptimizer;
import org.delia.db.schema.modify.SchemaMigrationPlanGenerator;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * Given the increasing complexity of schema migration, we need a new design.
 * 
 * @author Ian Rae
 *
 */
public class NewSchemaDesignTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		String src = "let x = Flight[1]";
		execute(src);
		
		DTypeRegistry registry = session.getExecutionContext().registry;
		SchemaDefinitionGenerator schemaDefGen = new SchemaDefinitionGenerator(registry, delia.getFactoryService());
		SchemaDefinition schema = schemaDefGen.generate();
		dumpObj("schema", schema);
		
		SchemaDeltaGenerator deltaGen = new SchemaDeltaGenerator(registry, delia.getFactoryService());
		SchemaDelta delta = deltaGen.generate(new SchemaDefinition(), schema);
		dumpObj("delta", delta);
		
		DBType dbType = delia.getDBInterface().getDBType();
		DatIdMap datIdMap = session.getDatIdMap();
		SchemaDeltaOptimizer optimizer = new SchemaDeltaOptimizer(registry, delia.getFactoryService(), dbType, datIdMap);
		delta = optimizer.optimize(delta);
		dumpObj("opt", delta);
		
		SchemaMigrationPlanGenerator plangen = new SchemaMigrationPlanGenerator(registry, delia.getFactoryService(), dbType);
		List<SchemaChangeOperation> ops = plangen.generate(delta);
		dumpObj("op", ops);
	}	

	//-------------------------
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: '4E/QIA=='}");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}
}
