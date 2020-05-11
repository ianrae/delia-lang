package org.delia.db.hls;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.db.QueryDetails;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;

public class HLSQuerySpan implements HLSElement {
	public DStructType fromType;
	public DType resultType;
	
	public MTElement mtEl;
	public FILElement filEl;
	public RElement rEl;
	public FElement fEl;
	public List<GElement> gElList = new ArrayList<>();
	public SUBElement subEl;
	public OLOElement oloEl;
	
	//fields added during sql generation
	public List<DValue> paramL = new ArrayList<>(); //for prepared statement
	public QueryDetails details;
	public String finalWhereSql;
	public List<RenderedField> renderedFieldL = new ArrayList<>();
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		String s4 = BuiltInTypes.convertDTypeNameToDeliaName(resultType.getName());
		String ss = String.format("%s->%s", fromType.getName(), s4);
		joiner.add(ss);
		
		if (mtEl != null) {
			joiner.add(mtEl.toString());
		}
		if (filEl != null) {
			joiner.add(filEl.toString());
		}
		if (rEl != null) {
			joiner.add(rEl.toString());
		}
		if (fEl != null) {
			joiner.add(fEl.toString());
		}
		StringJoiner subJ = new StringJoiner(",");
		for(GElement gel: gElList) {
			subJ.add(gel.toString());
		}
		String s3 = String.format("(%s)", subJ.toString());
		joiner.add(s3);
		
		if (subEl != null) {
			joiner.add(subEl.toString());
		}
		if (oloEl != null) {
			joiner.add(oloEl.toString());
		}
		
		String s = String.format("{%s}", joiner.toString());
		return s;
	}

	public boolean hasFunction(String fnName) {
		GElement gel = findFunction(fnName);
		return gel != null;
	}
	public GElement findFunction(String fnName) {
		if (gElList == null) {
			return null;
		}
		
		for(GElement gel: gElList) {
			if (gel.qfe.funcName.equals(fnName)) {
				return gel;
			}
		}
		return null;
	}
	
}