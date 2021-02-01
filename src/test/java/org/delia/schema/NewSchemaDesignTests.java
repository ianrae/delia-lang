package org.delia.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.schema.FieldInfo;
import org.delia.db.sizeof.DeliaTestBase;
import org.delia.hld.QueryBuilderHelper;
import org.delia.hld.simple.SimpleSqlBuilder;
import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.sprig.SprigService;
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
 * Given the increasing complexity of schema migration, we need a new design.
 * 
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
	
	
	public static class RegAwareServiceBase extends ServiceBase {
		protected DTypeRegistry registry;

		public RegAwareServiceBase(DTypeRegistry registry, FactoryService factorySvc) {
			super(factorySvc);
			this.registry = registry;
		}
	}	
	
	/**
	 * TODO
	 *  registry to SchemaDefinition
	 *  SchemaDefinition to/from json
	 *  Compare SchemaDefinition to SchemaDelta
	 *  SchemaDelta to MigrationPlan (series of SCA)
	 */
	
	public static class SchemaDefinitionGenerator extends RegAwareServiceBase {
		public static final int VERSION = 3;
		
		public SchemaDefinitionGenerator(DTypeRegistry registry, FactoryService factorySvc) {
			super(registry, factorySvc);
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
	
	//== Delta ==
	public static class SxFieldDelta {
		public String fieldName;
		public String fDelta; //null means no change. else is rename
		public String tDelta; //""
		public String flgsDelta; //""
		public Integer szDelta; //null means no change, else is new size
//		public int datId;  never changes
		public SxFieldInfo info; //when adding
		
		public SxFieldDelta(String fieldName) {
			this.fieldName = fieldName;
		}
	}	
	
	public static class SxTypeDelta {
		public String typeName; 
		public String nmDelta; //null means no change. else is rename
		public String baDelta; //""
		public List<SxFieldDelta> fldsI = new ArrayList<>();
		public List<SxFieldDelta> fldsU = new ArrayList<>();
		public List<SxFieldDelta> fldsD = new ArrayList<>();
		public SxTypeInfo info; //when adding a type
	}	
	
	public static class SxOtherDelta {
		public String name;
		public List<String> oldArgs;
		public List<String> newArgs;
	}	
	
	public static class SchemaDelta {
		public List<SxTypeDelta> typesI = new ArrayList<>();
		public List<SxTypeDelta> typesU = new ArrayList<>();
		public List<SxTypeDelta> typesD = new ArrayList<>();
		public List<SxOtherDelta> othersI = new ArrayList<>();
		public List<SxOtherDelta> othersU = new ArrayList<>();
		public List<SxOtherDelta> othersD = new ArrayList<>();
		
		public boolean isEmpty() {
			if (typesI.isEmpty() && typesU.isEmpty() && typesD.isEmpty()) {
				if (othersI.isEmpty() && othersD.isEmpty() && othersD.isEmpty()) {
					return true;
				}
			}
			return false;
		}
	}	

	public static class SchemaDeltaGenerator extends RegAwareServiceBase {

		public SchemaDeltaGenerator(DTypeRegistry registry, FactoryService factorySvc) {
			super(registry, factorySvc);
		}
		
		public SchemaDelta generate(SchemaDefinition schema1, SchemaDefinition schema2) {
			SchemaDelta delta = new SchemaDelta();
			
			List<SxTypeInfo> list2 = new ArrayList<>(schema2.types);
			for(SxTypeInfo tt: schema1.types) {
				SxTypeInfo sc2 = findIn(tt, schema2);
				if (sc2 != null) {
					SxTypeDelta td = buildTypeDelta(tt);
					diffFields(tt, sc2, delta, td);
					list2.remove(sc2);
					
					delta.typesU.add(td);
				} else {
					SxTypeDelta td = buildTypeDelta(tt);
					delta.typesD.add(td); //in list1 but not in list2
				}
			}

			for(SxTypeInfo tt: list2) {
				SxTypeDelta td = buildTypeDelta(tt);
				td.info = tt;
				delta.typesI.add(td);
			}
			
			//TODO: handle others

			return delta;
		}
		
		private SxTypeDelta buildTypeDelta(SxTypeInfo tt) {
			SxTypeDelta typeDelta = new SxTypeDelta();
			typeDelta.typeName = tt.nm;
			// TODO Auto-generated method stub
			return typeDelta;
		}

		private SxTypeInfo findIn(SxTypeInfo tt, SchemaDefinition schema2) {
			Optional<SxTypeInfo> opt = schema2.types.stream().filter(x -> x.nm.equals(tt.nm)).findAny();
			return opt.orElse(null);
		}
		
		private void diffFields(SxTypeInfo tt, SxTypeInfo sc2, SchemaDelta delta, SxTypeDelta td) {
//			private void diffFields(SchemaType st1, SchemaType st2, List<SchemaType> diffList) {
			
			List<SxFieldInfo> flist1 = tt.flds;
			List<SxFieldInfo> flist2 = sc2.flds;
			List<SxFieldInfo> list2 = new ArrayList<>(flist2);
			
			//TODO support rename
			//add fieldinfo.fieldIndex - index of field within dtype
			//if I and D have same fieldIndex and same type and options then do a rename

			//I/U/D/R/A/AT/ASZ/ASN
			for(SxFieldInfo finfo: flist1) {
				SxFieldInfo f2 = findFieldIn(finfo, flist2);
				if (f2 != null) {
					if (!finfo.t.equals(f2.t)) {
						SxFieldDelta fd = new SxFieldDelta(finfo.f);
						fd.tDelta = f2.t;
						td.fldsU.add(fd);
					} else {
						Integer sizeofDelta = calcSizeofString(finfo, f2);
						if (sizeofDelta != null) {
							SxFieldDelta fd = new SxFieldDelta(finfo.f);
							fd.szDelta = sizeofDelta;
							td.fldsU.add(fd);
						}
					}
					
					String deltaFlags = calcDeltaFlags(finfo, f2);
					if (!StringUtil.isNullOrEmpty(deltaFlags)) {
						SxFieldDelta fd = new SxFieldDelta(finfo.f);
						fd.flgsDelta = deltaFlags;
						td.fldsU.add(fd);
					}
					
					list2.remove(f2);
				} else {
					SxFieldDelta fd = new SxFieldDelta(finfo.f);
					td.fldsD.add(fd);
				}
			}

			for(SxFieldInfo f2: list2) {
				//in list2 but not in list1
				SxFieldDelta fd = new SxFieldDelta(f2.f);
				fd.info = f2;
				td.fldsI.add(fd);
			}
		}

		private Integer calcSizeofString(SxFieldInfo finfo, SxFieldInfo f2) {
			if (finfo.sz == f2.sz) {
				return null;
			}
			
			return f2.sz; // new size value
		}

		/** generate a string of modifier changes.
		 *  +O means adding optional
		 *  -O means removing optional
		 *  -also +U,-U,+P,-P,+S,-S
		 */
		private String calcDeltaFlags(SxFieldInfo finfo, SxFieldInfo f2) {
			StringJoiner joiner = new StringJoiner(",");
			for(int i = 0; i < finfo.flgs.length(); i++) {
				char ch = finfo.flgs.charAt(i);
				if (f2.flgs.indexOf(ch) < 0) { //in f1 and not in f2?
					joiner.add(String.format("-%c", ch));
				}
			}		
			
			for(int i = 0; i < f2.flgs.length(); i++) {
				char ch = f2.flgs.charAt(i);
				if (finfo.flgs.indexOf(ch) < 0) { //not in f1 and in f2?
					joiner.add(String.format("+%c", ch));
				}
			}		
			
			return joiner.toString();
		}

		private SxFieldInfo findFieldIn(SxFieldInfo target, List<SxFieldInfo> flist2) {
			for(SxFieldInfo f2: flist2) {
				if (target.f.equals(f2.f)) {
					return f2;
				}
			}
			return null;
		}
	}
	
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
