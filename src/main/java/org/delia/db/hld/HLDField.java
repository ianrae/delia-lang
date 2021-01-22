package org.delia.db.hld;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;

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
		public int columnIndex;
		private TypePair pair;

		@Override
		public String toString() {
			String fldType = BuiltInTypes.convertDTypeNameToDeliaName(fieldType.getName());
			String s = String.format("%s.%s(%s,%s)", structType.getName(), fieldName, fldType, alias);
			return s;
		}
		
		public String render() {
			if (alias == null) {
				return fieldName;
			} else {
				return String.format("%s.%s", alias, fieldName); //TODO do as cust later
			}
		}

		public TypePair getAsPair() {
			if (pair == null) {
				pair = new TypePair(fieldName, fieldType);
			}
			return pair;
		}
		
	}