package org.delia.db.schema.modify;

/**
	 * TODO
	 *  registry to SchemaDefinition
	 *  SchemaDefinition to/from json
	 *  Compare SchemaDefinition to SchemaDelta
	 *  SchemaDelta to MigrationPlan (series of SCA)
	 */
	
	//== Delta ==
	public class SxFieldDelta {
		public String fieldName;
		public String fDelta; //null means no change. else is rename
		public String tDelta; //""
		public String flgsDelta; //""
		public Integer szDelta; //null means no change, else is new size
//		public int datId;  never changes
		public SxFieldInfo info; //when adding
		public String typeNamex;
		
		public SxFieldDelta(String fieldName, String typeName) {
			this.fieldName = fieldName;
			this.typeNamex = typeName;
		}
	}