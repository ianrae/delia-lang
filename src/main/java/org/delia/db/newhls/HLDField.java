package org.delia.db.newhls;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;

/**
	 * Represents a field mentioned in a query. Has all the info for rendering SQL or doing MEM query.
	 * @author ian
	 *
	 */
	public class HLDField {
		public DStructType structType;
		public String fieldName;
		public DType fieldType;
		//		public boolean isAssocField;
//		public String groupName; just use alias??
		public String alias;
		public String asStr;
		
		public Object source; //null means main struct, else is joinelement

		@Override
		public String toString() {
			String fldType = BuiltInTypes.convertDTypeNameToDeliaName(fieldType.getName());
			String s = String.format("%s.%s(%s,%s)", structType.getName(), fieldName, fldType, alias);
			return s;
		}
	}