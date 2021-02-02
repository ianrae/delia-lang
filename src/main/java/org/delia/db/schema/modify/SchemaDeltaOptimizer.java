package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.db.DBType;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DRuleHelper;

/**
 * Improves a schema delta by recognizing things such as Delete + Insert and
 * replacing them with a rename.
 * 
 * @author ian
 *
 */
public class SchemaDeltaOptimizer extends RegAwareServiceBase {
	private boolean isMemDB;

	public SchemaDeltaOptimizer(DTypeRegistry registry, FactoryService factorySvc, DBType dbType) {
		super(registry, factorySvc);
		this.isMemDB = DBType.MEM.equals(dbType);
	}

	public SchemaDelta optimize(SchemaDelta delta) {
		detectTableRename(delta);
		detectFieldRename(delta);
		removeParentRelations(delta);
		detectOneToManyFieldChange(delta);

		return delta;
	}

	private void detectTableRename(SchemaDelta delta) {
		List<SxTypeDelta> newlist = new ArrayList<>();
		List<SxTypeDelta> doomedL = new ArrayList<>();
		for(SxTypeDelta st: delta.typesD) {
			SxTypeDelta stOther = findMatchingTableInsert(delta, st);
			if (stOther != null) {
				SxTypeDelta td = new SxTypeDelta(st.typeName);
				td.nmDelta = stOther.typeName;
				delta.typesU.add(td);

				delta.typesI.remove(stOther);
				doomedL.add(st);
				log.log("migrate: '%s' -> '%s' replace with rename.", st.typeName, stOther.typeName);
			}
			newlist.add(st);
		}

		for(SxTypeDelta doomed: doomedL) {
			delta.typesD.remove(doomed);
		}
	}

	private void detectFieldRename(SchemaDelta delta) {
		for(SxTypeDelta td: delta.typesU) {
			doDetectFieldRename(td);
		}
	}
	private void doDetectFieldRename(SxTypeDelta td) {	
		List<SxFieldDelta> doomedL = new ArrayList<>();
		for(SxFieldDelta st: td.fldsD) {
			SxFieldDelta stOther = findMatchingFieldInsertForRename(td, st);
			if (stOther != null) {
				SxFieldDelta fd = new SxFieldDelta(st.fieldName, td.typeName);
				fd.fDelta = stOther.fieldName;
				td.fldsU.add(fd);

				td.fldsI.remove(stOther);
				doomedL.add(st);
				log.log("migrate: '%s.%s' -> '%s.%s' replace with rename.", td.typeName, st.fieldName, td.typeName, st.fieldName);
			}
		}

		for(SxFieldDelta doomed: doomedL) {
			td.fldsD.remove(doomed);
		}
	}
	/**
	 * In 1-to-1 and 1-to-many the parent side of a relation doesn't exist in the
	 * db, so remove steps for them.
	 * 
	 * @param diffL
	 * @return
	 */
	private void removeParentRelations(SchemaDelta delta) {
		if (isMemDB) {
			return; //we need to modify parent relations too in MEM db
		}

		List<SxTypeDelta> combinedList = new ArrayList<>(delta.typesI);
		combinedList.addAll(delta.typesU);

		List<SxFieldDelta> newlist = new ArrayList<>();
		List<SxFieldDelta> manyToManyList = new ArrayList<>();

		for(SxTypeDelta st: combinedList) {
			List<SxFieldDelta> doomedList = new ArrayList<>();
			for(SxFieldDelta fd: st.fldsI) {
				RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeName, fd.fieldName, registry);
				RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, fd.fieldName, registry);
				if (ruleOne != null && ruleOne.isParent()) {
					//don't add
				} else 	if (ruleMany != null) {
					if (ruleMany.relInfo.isManyToMany()) {
						if (! findOtherSideOfRelation(manyToManyList, fd)) {
							newlist.add(fd);
							manyToManyList.add(fd);
						}
					} else {
						//don't add (many side is always a parent)
						doomedList.add(fd);
					}
				}
			}
			doomedList = removeDoomed(st.fldsI, doomedList);

			for(SxFieldDelta fd: st.fldsU) {
				if (fd.fDelta != null) { //field rename
					RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeName, fd.fDelta, registry);
					RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeName, fd.fDelta, registry);
					if (ruleOne != null && ruleOne.isParent()) {
						//don't add
						doomedList.add(fd);
					} else 	if (ruleMany != null) {
						if (ruleMany.relInfo.isManyToMany()) {
							//do nothing - field names not in assoc table
						} else {
							//don't add (many side is always a parent)
							doomedList.add(fd);
						}
					}
				}
			} 
			doomedList = removeDoomed(st.fldsU, doomedList);

			for(SxFieldDelta fd: st.fldsD) {
				//relation codes
				// a - relation one parent
				// b - relation one         (child)
				// c = relation many parent
				// d = relation many        (child) -can this occur?
				String flags = fd.info.flgs;
				if (flags != null && (flags.contains("a") || flags.contains("c"))) {
					doomedList.add(fd);
				} else {
				}
			}
			doomedList = removeDoomed(st.fldsD, doomedList);
		}
	}

	private List<SxFieldDelta> removeDoomed(List<SxFieldDelta> list, List<SxFieldDelta> doomedList) {
		for(SxFieldDelta fd: doomedList) {
			list.remove(fd);
		}
		doomedList.clear();
		return doomedList;
	}

	private boolean findOtherSideOfRelation(List<SxFieldDelta> manyToManyList, SxFieldDelta target) {
		for(SxFieldDelta st: manyToManyList) {
			RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeNamex, st.fieldName, registry);
			if (ruleMany != null) {
				if (ruleMany.relInfo.farType.getName().equals(target.typeNamex)) {
					return true;
				}
			}
		}
		return false;
	}

	private void detectOneToManyFieldChange(SchemaDelta delta) {
		for(SxTypeDelta td: delta.typesU) {
			doDetectOneToManyFieldChange(td, delta);
		}
	}
	private void doDetectOneToManyFieldChange(SxTypeDelta td, SchemaDelta delta) {
		for(SxFieldDelta st: td.fldsU) {
			if (st.flgsDelta == null) {
				continue;
			}

			if (st.flgsDelta.equals("-a,+c")) { //changing parent from one to many?
				RelationManyRule ruleMany = DRuleHelper.findManyRule(st.typeNamex, st.fieldName, registry);
				//					DType farType = ruleMany.relInfo.farType;
				//					DStructType nearType = ruleMany.relInfo.nearType;
				RelationInfo otherSide = ruleMany.relInfo.otherSide; //DRuleHelper.findOtherSideOneOrMany(farType, nearType);

				SxTypeDelta otherTd = findOther(delta, otherSide.nearType);
				SxFieldDelta newfd = new SxFieldDelta(otherSide.fieldName, otherTd.typeName);
				//					st.action = "A";
				//					st.field = otherSide.fieldName;
				//					st.typeName = otherSide.nearType.getName();
				newfd.flgsDelta = "-U"; //remove UNIQUE
				otherTd.fldsU.add(newfd);

				log.log("migrate: one to many on '%s.%s'", st.typeNamex, st.fieldName);
			} else if (st.flgsDelta.equals("-c,+a")) { //changing parent from many to one?
				RelationOneRule ruleOne = DRuleHelper.findOneRule(st.typeNamex, st.fieldName, registry);
				//					DType farType = ruleOne.relInfo.farType;
				//					DStructType nearType = ruleOne.relInfo.nearType;
				RelationInfo otherSide = ruleOne.relInfo.otherSide; //DRuleHelper.findOtherSideOneOrMany(farType, nearType);

				SxTypeDelta otherTd = findOther(delta, otherSide.nearType);
				SxFieldDelta newfd = new SxFieldDelta(otherSide.fieldName, otherTd.typeName);
				//					st.action = "A";
				//					st.field = otherSide.fieldName;
				//					st.typeName = otherSide.nearType.getName();
				newfd.flgsDelta = "+U"; //remove UNIQUE
				otherTd.fldsU.add(newfd);

				log.log("migrate: one to many on '%s.%s'", st.typeNamex, st.fieldName);
			}
		}
	}

	private SxTypeDelta findOther(SchemaDelta delta, DStructType nearType) {
		String target = nearType.getName();
		Optional<SxTypeDelta> opt = delta.typesU.stream().filter(x -> x.typeName.equals(target)).findAny();
		return opt.orElse(null);
	}

	private SxTypeDelta findMatchingTableInsert(SchemaDelta delta, SxTypeDelta stTarget) {
		int count = 0;
		SxTypeDelta match = null;
		for(SxTypeDelta st: delta.typesI) {
			if (st.typeName.equals(st.typeName)) {
				match = st;
				count++;
			}
		}

		if (match != null && count == 1) {
			return match;
		}
		return null;
	}

	/**
	 * Do a heuristic match for potential renames. Match everything except fieldName
	 * @param td
	 * @param fdTarget
	 * @return
	 */
	private SxFieldDelta findMatchingFieldInsertForRename(SxTypeDelta td, SxFieldDelta fdTarget) {
		int count = 0;
		SxFieldDelta match = null;
		for(SxFieldDelta st: td.fldsI) {
			if (st.info.t.equals(fdTarget.info.t)) {
				if (st.info.flgs.equals(fdTarget.info.flgs)) {
					match = st;
					count++;
				}						
			}
		}

		if (match != null && count == 1) {
			return match;
		}
		return null;
	}
}