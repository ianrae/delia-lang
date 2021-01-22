package org.delia.db.hld;

import org.delia.assoc.DatIdMap;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

/**
 *  -when generating t0.foo a bunch of things can happen
 *  -t0.addr  if addr is parent then flip to t1.cust
 *  -t0.addr  if M:M then flip to t1.leftv
 *  -t1.y  if 2nd join (MM and fetch) then flip to t2.y
 * @author ian
 *
 */
public class SqlColumnBuilder {

	private DatIdMap datIdMap;
	public SqlColumnBuilder(DatIdMap datIdMap) {
		this.datIdMap = datIdMap;
	}
	public SqlColumn adjust(String alias, DStructType structType, String fieldName, JoinElement el) {
		if (el.relinfo.isManyToMany()) {
			String field;
			if (el.relinfo.nearType == structType) {
				if (el.relinfo.fieldName.equals(fieldName)) {
					field = datIdMap.getAssocOtherField(el.relinfo);
				} else {
					field = datIdMap.getAssocFieldFor(el.relinfo);
				}
			} else {
				if (el.relinfo.fieldName.equals(fieldName)) {
					field = datIdMap.getAssocOtherField(el.relinfo);
				} else {
					field = datIdMap.getAssocFieldFor(el.relinfo);
				}
			}
			return new SqlColumn(el.aliasName, field);
		} else if (el.relinfo.isParent) {
			if (el.relinfo.fieldName.equals(fieldName)) {
				TypePair pkpair = el.getOtherSidePK();
				return new SqlColumn(el.aliasName, pkpair.name);
			} else {
				return new SqlColumn(alias, fieldName);
			}
		} else {
			return new SqlColumn(alias, fieldName);
		}
	}
	public SqlColumn adjustScalar(String alias, String fieldName, DStructType structType, JoinElement el) {
		if (el.relinfo.isManyToMany()) {
			String field;
			if (el.aliasNameAdditional != null) {
				return new SqlColumn(el.aliasNameAdditional, fieldName);
			} else if (el.relinfo.nearType == structType) {
				if (el.relinfo.fieldName.equals(fieldName)) {
					field = datIdMap.getAssocFieldFor(el.relinfo);
				} else {
					field = datIdMap.getAssocOtherField(el.relinfo);
				}
			} else {
				if (el.relinfo.fieldName.equals(fieldName)) {
					field = datIdMap.getAssocFieldFor(el.relinfo);
				} else {
					field = datIdMap.getAssocOtherField(el.relinfo);
				}
			}
			return new SqlColumn(el.aliasName, field);
		} else if (el.relinfo.isParent) {
			if (el.relinfo.fieldName.equals(fieldName)) {
				TypePair pkpair = el.getOtherSidePK();
				return new SqlColumn(el.aliasName, pkpair.name);
			} else {
				return new SqlColumn(alias, fieldName);
			}
		} else {
			return new SqlColumn(alias, fieldName);
		}
	}
}
