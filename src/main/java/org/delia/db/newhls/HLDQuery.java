package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.db.newhls.cond.FilterCond;
import org.delia.type.DStructType;
import org.delia.type.DType;

/**
 * Main class to represent a query. Should have all info to render sql or to invoke MEM.
 * Goal is to eventually be able to cache this to avoid repeated creation.
 * @author ian
 *
 */
public class HLDQuery {
	public DStructType fromType;
	public String fromAlias;
	public DStructType mainStructType; //C[].addr then fromType is A and mainStringType is C
	public DType resultType; //might be string if .firstName
	public FilterCond filter;
	public List<RelationField> throughChain = new ArrayList<>();
	public StructField finalField; //eg Customer.addr
	public List<FetchSpec> fetchL = new ArrayList<>(); //order matters: eg. .addr.fetch('country')
	public List<QueryFnSpec> funcL = new ArrayList<>(); //list and calc fns. order matters: eg. .addr.first().city
	public List<HLDField> fieldL = new ArrayList<>(); 

	//added after
	public List<JoinElement> joinL = new ArrayList<>();

	@Override
	public String toString() {
		String s = String.format("%s:%s[]:%s", resultType.getName(), fromType.getName(), mainStructType.getName());
		s += String.format(" [%s]", filter.toString());

		if (throughChain.isEmpty()) {
			s += " TC[]";
		} else {
			StringJoiner joiner = new StringJoiner(",");
			for(RelationField sf: throughChain) {
				joiner.add(sf.toString());
			}
			s += String.format(" TC[%s]", joiner.toString());
		}

		if (finalField == null) {
			s += " fld()";
		} else {
			s += String.format(" fld(%s)", finalField.toString());
		}

		if (fetchL.isEmpty()) {
			s += " fetch[]";
		} else {
			StringJoiner joiner = new StringJoiner(",");
			for(FetchSpec sf: fetchL) {
				joiner.add(sf.toString());
			}
			s += String.format(" fetch[%s]", joiner.toString());
		}

		if (funcL.isEmpty()) {
			s += " fn[]";
		} else {
			StringJoiner joiner = new StringJoiner(",");
			for(QueryFnSpec sf: funcL) {
				joiner.add(sf.toString());
			}
			s += String.format(" fn[%s]", joiner.toString());
		}

		if (fieldL.isEmpty()) {
			s += " {}";
		} else {
			StringJoiner joiner = new StringJoiner(",");
			for(HLDField rf: fieldL) {
				joiner.add(rf.toString());
			}
			s += String.format(" {%s}", joiner.toString());
		}
		return s;
	}
}