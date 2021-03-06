//package org.delia.db.schema;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.lang3.StringUtils;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.DBType;
//import org.delia.relation.RelationCardinality;
//import org.delia.relation.RelationInfo;
//import org.delia.rule.rules.RelationManyRule;
//import org.delia.rule.rules.RelationOneRule;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.util.DRuleHelper;
//
//public class MigrationOptimizer extends ServiceBase {
//
//	private DTypeRegistry registry;
//	private boolean isMemDB;
//
//	public MigrationOptimizer(FactoryService factorySvc, DTypeRegistry registry, DBType dbType) {
//		super(factorySvc);
//		this.registry = registry;
//		this.isMemDB = DBType.MEM.equals(dbType);
//	}
//	
//	public List<SchemaType> optimizeDiffs(List<SchemaType> diffL) {
//		diffL = detectTableRename(diffL);
//		diffL = detectFieldRename(diffL);
//		diffL = removeParentRelations(diffL);
//		diffL = detectOneToManyFieldChange(diffL);
//		
//		return diffL;
//	}
//	
//	/**
//	 * In 1-to-1 and 1-to-many the parent side of a relation doesn't exist in the
//	 * db, so remove steps for them.
//	 * 
//	 * @param diffL
//	 * @return
//	 */
//	private List<SchemaType> removeParentRelations(List<SchemaType> diffL) {
//		if (isMemDB) {
//			return diffL; //we need to modify parent relations too in MEM db
//		}
//		
//		List<SchemaType> newlist = new ArrayList<>();
//		List<SchemaType> manyToManyList = new ArrayList<>();
//		for(SchemaType st: diffL) {
//			if (st.isFieldInsert()) {
//				RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeName, st.field, registry);
//				RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, st.field, registry);
//				if (ruleOne != null && ruleOne.isParent()) {
//					//don't add
//				} else 	if (ruleMany != null) {
//					if (ruleMany.relInfo.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
//						if (! findOtherSideOfRelation(manyToManyList, st)) {
//							newlist.add(st);
//							manyToManyList.add(st);
//						}
//					} else {
//						//don't add (many side is always a parent)
//					}
//				} else {
//					newlist.add(st);
//				}
//			} else if (st.isFieldRename()) {
//				RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeName, st.newName, registry);
//				RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, st.newName, registry);
//				if (ruleOne != null && ruleOne.isParent()) {
//					//don't add
//				} else 	if (ruleMany != null) {
//					if (ruleMany.relInfo.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
//						//do nothing - field names not in assoc table
//					} else {
//						//don't add (many side is always a parent)
//					}
//				} else {
//					newlist.add(st);
//				}
//			} else if (st.isFieldDelete()) {
//				FieldInfo f1 = parseFieldInfo(st);
//				//relation codes
//				// a - relation one parent
//				// b - relation one         (child)
//				// c = relation many parent
//				// d = relation many        (child) -can this occur?
//				String flags = f1.flagStr;
//				if (flags.contains("a") || flags.contains("c")) {
//				} else {
//					newlist.add(st);
//				}
//			} else {
//				newlist.add(st);
//			}
//		}
//		return newlist;
//	}
//	
//	private boolean findOtherSideOfRelation(List<SchemaType> manyToManyList, SchemaType target) {
//		for(SchemaType st: manyToManyList) {
//			RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, st.field, registry);
//			if (ruleMany != null) {
//				if (ruleMany.relInfo.farType.getName().equals(target.typeName)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//
//	private List<SchemaType> detectOneToManyFieldChange(List<SchemaType> diffL) {
//		List<SchemaType> newlist = new ArrayList<>();
//		List<SchemaType> doomedL = new ArrayList<>();
//		for(SchemaType st: diffL) {
//			if (st.isFieldAlter()) {
//				if (st.newName.equals("-a,+c")) { //changing parent from one to many?
//					RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, st.field, registry);
//					DType farType = ruleMany.relInfo.farType;
//					DStructType nearType = ruleMany.relInfo.nearType;
//					RelationInfo otherSide = ruleMany.relInfo.otherSide; //DRuleHelper.findOtherSideOneOrMany(farType, nearType);
//
//					st.action = "A";
//					st.field = otherSide.fieldName;
//					st.typeName = otherSide.nearType.getName();
//					st.newName = "-U"; //remove UNIQUE
//					
//					log.log("migrate: one to many on '%s.%s'", st.typeName, st.field);
//				} else if (st.newName.equals("-c,+a")) { //changing parent from many to one?
//					RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeName, st.field, registry);
//					DType farType = ruleOne.relInfo.farType;
//					DStructType nearType = ruleOne.relInfo.nearType;
//					RelationInfo otherSide = ruleOne.relInfo.otherSide; //DRuleHelper.findOtherSideOneOrMany(farType, nearType);
//
//					st.action = "A";
//					st.field = otherSide.fieldName;
//					st.typeName = otherSide.nearType.getName();
//					st.newName = "+U"; //remove UNIQUE
//					
//					log.log("migrate: one to many on '%s.%s'", st.typeName, st.field);
//				}
//				newlist.add(st);
//			} else {
//				newlist.add(st);
//			}
//		}
//		
//		for(SchemaType doomed: doomedL) {
//			newlist.remove(doomed);
//		}
//		
//		return newlist;
//	}
//	
//	private List<SchemaType> detectTableRename(List<SchemaType> diffL) {
//		List<SchemaType> newlist = new ArrayList<>();
//		List<SchemaType> doomedL = new ArrayList<>();
//		for(SchemaType st: diffL) {
//			if (st.isTblDelete()) {
//				SchemaType stOther = findMatchingTableInsert(diffL, st);
//				if (stOther != null) {
//					st.action = "R";
//					st.newName = stOther.typeName;
//					doomedL.add(stOther);
//					log.log("migrate: '%s' -> '%s' replace with rename.", st.typeName, stOther.typeName);
//				}
//				newlist.add(st);
//			} else {
//				newlist.add(st);
//			}
//		}
//		
//		for(SchemaType doomed: doomedL) {
//			newlist.remove(doomed);
//		}
//		
//		return newlist;
//	}
//
//	public List<SchemaType> detectFieldRename(List<SchemaType> diffL) {
//		List<SchemaType> newlist = new ArrayList<>();
//		List<SchemaType> doomedL = new ArrayList<>();
//		for(SchemaType st: diffL) {
//			if (st.isFieldDelete()) {
//				SchemaType stOther = findMatchingFieldInsert(diffL, st.typeName, st.field);
//				if (stOther != null) {
//					FieldInfo f1 = parseFieldInfo(st);
//					FieldInfo f2 = parseFieldInfo(stOther);
//					if (f1.type.equals(f2.type) && f1.flagStr.equals(f2.flagStr)) {
//						st.action = "R";
//						st.newName = stOther.field;
//						doomedL.add(stOther);
//						log.log("migrate: '%s.%s' -> '%s.%s' replace with rename.", st.typeName, st.field, stOther.typeName, st.newName);
//					}
//				}
//				newlist.add(st);
//			} else {
//				newlist.add(st);
//			}
//		}
//		
//		for(SchemaType doomed: doomedL) {
//			newlist.remove(doomed);
//		}
//		
//		return newlist;
//	}
//
//	private FieldInfo parseFieldInfo(SchemaType st) {
//		List<FieldInfo> flist1 = parseFields(st);
//		FieldInfo f1 = this.findFieldIn(st.field, flist1);
//		return f1;
//	}
//
//	private SchemaType findMatchingFieldInsert(List<SchemaType> diffL, String typeName, String field) {
//		int count = 0;
//		SchemaType match = null;
//		for(SchemaType st: diffL) {
//			if (st.isFieldInsert() && st.typeName.equals(typeName)) {
//				match = st;
//				count++;
//			}
//		}
//		
//		if (match != null && count == 1) {
//			return match;
//		}
//		return null;
//	}
//	private SchemaType findMatchingTableInsert(List<SchemaType> diffL, SchemaType st2) {
//		int count = 0;
//		SchemaType match = null;
//		for(SchemaType st: diffL) {
//			if (st.isTblInsert()) {
//				String suffix = st.line.substring(st.line.indexOf(':'));
//				String suffix2 = st2.line.substring(st2.line.indexOf(':'));
//				if (suffix.equals(suffix2)) {
//					match = st;
//					count++;
//				}
//			}
//		}
//		
//		if (match != null && count == 1) {
//			return match;
//		}
//		return null;
//	}
//
//	private FieldInfo findFieldIn(String fieldName, List<FieldInfo> flist2) {
//		for(FieldInfo f2: flist2) {
//			if (fieldName.equals(f2.name)) {
//				return f2;
//			}
//		}
//		return null;
//	}
//
//	private List<FieldInfo> parseFields(SchemaType schema1) {
//		//Customer:struct:{id:int:P,wid:int:}
//		List<FieldInfo> list = new ArrayList<>();
//		String s = StringUtils.substringAfter(schema1.line, "{");
//		s = StringUtils.substringBeforeLast(s, "}");
//		String[] ar = s.split(",");
//		for(String ss: ar) {
//			FieldInfo finfo = new FieldInfo();
//			finfo.name = StringUtils.substringBefore(ss, ":");
//			finfo.type = StringUtils.substringBetween(ss, ":", ":");
//			String tmp = StringUtils.substringAfterLast(ss, ":");
//			finfo.flagStr = StringUtils.substringBefore(tmp, "/");
//			list.add(finfo);
//		}
//		return list;
//	}
//
//	private SchemaType findIn(SchemaType st, List<SchemaType> list2) {
//		for(SchemaType schema2: list2) {
//			if (st.typeName.equals(schema2.typeName)) {
//				return schema2;
//			}
//		}
//		return null;
//	}
//
//}