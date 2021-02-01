package org.delia.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class NewSchemaDesignTests extends DeliaTestBase { 
	
	public static class SxFieldInfo {
		public String f;
		public String t;
		public String flgs;
		public int sz;
		public int datId;
	}	
	public static class SxOtherInfo {
		public String ct; //type of change
		public List<String> args;
	}	
	
	public static class SxTypeInfo {
		public String nm;
		public String ba;
		public List<SxFieldInfo> flds = new ArrayList<>();
	}	
	public static class SchemaDefinition {
		public int ver;
		public List<SxTypeInfo> types = new ArrayList<>();
		public List<SxOtherInfo> others = new ArrayList<>();
	}	
	
	/**
	 * TODO
	 *  registry to SchemaDefinition
	 *  SchemaDefinition to/from json
	 *  Compare SchemaDefinition to SchemaDelta
	 *  SchemaDelta to MigrationPlan (series of SCA)
	 */
	
	public static class SchemaDefinitionGenerator {
		public static final int VERSION = 3;
		
		private DTypeRegistry registry;

		public SchemaDefinitionGenerator(DTypeRegistry registry) {
			this.registry = registry;
		}
		
		public SchemaDefinition generate() {
			SchemaDefinition schema = new SchemaDefinition();
			schema.ver = VERSION;
			
			//because of re-executing with forward decls some types are in registry.orderedList twice
			//use a map to ensure only do each type once
			Map<DType,DType> dupMap = new ConcurrentHashMap<>();
			
			List<DType> list = registry.getOrderedList();
			for(DType type: list) {
				if (isBuiltInType(type) || !type.isStructShape()) {
					continue;
				}
				
				type = registry.getType(type.getName()); //get the real one (avoid earlier ones form re-execution
				if (dupMap.containsKey(type)) {
					continue;
				}
				dupMap.put(type, type);
				
				SxTypeInfo typeInfo = new SxTypeInfo();
				typeInfo.nm = type.getName();
				typeInfo.ba = type.getBaseType() == null ? "" : type.getBaseType().getName();
				
				int i = 0;
				if (type.isStructShape()) {
					DStructType dtype = (DStructType) type;
					for(TypePair pair: dtype.getAllFields()) {
						typeInfo.flds.add(genField(dtype, pair));
						i++;
					}
					
					addUniqueFieldsConstraints(dtype, schema);
				}
				
				schema.types.add(typeInfo);
			}
			
			return schema;
		}
		
		private void addUniqueFieldsConstraints(DType type, SchemaDefinition schema) {
			for(DRule rule: type.getRawRules()) {
				if (rule instanceof UniqueFieldsRule) {
					UniqueFieldsRule ufr = (UniqueFieldsRule) rule;
					List<String> list = ufr.getOperList().stream().map(x -> x.getSubject()).collect(Collectors.toList());
					String s = String.format("UFR(%s)", StringUtil.flatten(list));
					
					SxOtherInfo otherInfo = new SxOtherInfo();
					otherInfo.args = list;
					otherInfo.ct = "UF";
					schema.others.add(otherInfo);
				}
			}
		}
		
		
		
		private boolean isBuiltInType(DType type) {
			for(BuiltInTypes bintype: BuiltInTypes.values()) {
				if (type.getName().equals(bintype.name())) {
					return true;
				}
			}
			return false;
		}
		
		private SxFieldInfo genField(DStructType dtype, TypePair pair) {
			String flags = "";
			if (dtype.fieldIsOptional(pair.name)) {
				flags += "O";
			}
			if (dtype.fieldIsPrimaryKey(pair.name)) {
				flags += "P";
			}
			if (dtype.fieldIsUnique(pair.name)) {
				flags += "U";
			}
			if (dtype.fieldIsSerial(pair.name)) {
				flags += "S";
			}
			
			
			//relation codes
			// a - relation one parent
			// b - relation one         (child)
			// c = relation many parent
			// d = relation many        (child) -can this occur?
			int datId = 0;
			RelationOneRule oneRule = DRuleHelper.findOneRule(dtype.getName(), pair.name, registry);
			if (oneRule != null) {
				flags += oneRule.relInfo.isParent ? "a" : "b"; 
				datId = oneRule.relInfo.getDatId() == null ? 0 : oneRule.relInfo.getDatId();
			} else {
				RelationManyRule manyRule = DRuleHelper.findManyRule(dtype.getName(), pair.name, registry);
				if (manyRule != null) {
					flags += manyRule.relInfo.isParent ? "c" : "d"; 
					datId = manyRule.relInfo.getDatId() == null ? 0 : manyRule.relInfo.getDatId();
				}
			}
			
			SxFieldInfo fieldInfo = new SxFieldInfo();
			fieldInfo.f = pair.name;
			fieldInfo.flgs = flags;
			fieldInfo.t = getTypeAsString(pair);
			fieldInfo.sz = calcSizeofStr(dtype, pair);
			fieldInfo.datId = datId;
			return fieldInfo;
		}
		private int calcSizeofStr(DStructType dtype, TypePair pair) {
			int n = DRuleHelper.getSizeofField(dtype, pair.name);
			if (n != 0) {
				return n;
			}
			return 0;
		}

		private String getTypeAsString(TypePair pair) {
			try {
				BuiltInTypes fieldType = BuiltInTypes.valueOf(pair.type.getName());
				return BuiltInTypes.getDeliaTypeName(fieldType);
			} catch (Exception e) {
			}
			return pair.type.getName();
		}
		

	}
	
	
	@Test
	public void test() {
		String src = "let x = Flight[1]";
		execute(src);
		SchemaDefinitionGenerator schemaDefGen = new SchemaDefinitionGenerator(session.getExecutionContext().registry);
		SchemaDefinition schema = schemaDefGen.generate();
		dumpObj("schema", schema);
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
