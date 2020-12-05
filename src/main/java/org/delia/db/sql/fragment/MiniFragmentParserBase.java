package org.delia.db.sql.fragment;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

//single use!!!
	public class MiniFragmentParserBase extends ServiceBase implements TableFragmentMaker {
		private int nextAliasIndex = 0;
		private QueryTypeDetector queryDetectorSvc;
		private DTypeRegistry registry;
		protected WhereFragmentGenerator whereGen;
		private AliasCreator aliasCreator;
		
		public MiniFragmentParserBase(FactoryService factorySvc, DTypeRegistry registry, WhereFragmentGenerator whereGen, AliasCreator aliasCreator) {
			super(factorySvc);
			
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			this.whereGen = whereGen;
			this.aliasCreator = aliasCreator;
		}		
		public void createAlias(TableFragment frag) {
			if (aliasCreator != null) {
				aliasCreator.fillInAlias(frag);
			} else {
				char ch = (char) ('a' + nextAliasIndex++);
				frag.alias = String.format("%c", ch);
			}
		}

		@Override
		public TableFragment createTable(DStructType structType, StatementFragmentBase selectFrag) {
			TableFragment tblFrag = selectFrag.findByTableName(structType.getName());
			if (tblFrag != null) {
				return tblFrag;
			}
			
			tblFrag = new TableFragment();
			tblFrag.structType = structType;
			tblFrag.name = structType.getName();
			createAlias(tblFrag);
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			return tblFrag;
		}
		@Override
		public TableFragment createAssocTable(StatementFragmentBase selectFrag, String tableName) {
			TableFragment tblFrag = selectFrag.findByTableName(tableName);
			if (tblFrag != null) {
				return tblFrag;
			}
			
			tblFrag = new TableFragment();
			tblFrag.structType = null;
			tblFrag.name = tableName;
			createAlias(tblFrag);
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			return tblFrag;
		}
		

		protected void initWhere(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
			
			QueryType queryType = queryDetectorSvc.detectQueryType(spec);
			switch(queryType) {
			case ALL_ROWS:
			{
//				addWhereExist(sc, spec);
			}
				break;
			case OP:
//				addWhereClauseOp(sc, spec, typeName, tbl, statement);
				whereGen.addWhereClauseOp(spec, structType, selectFrag);
				break;
			case PRIMARY_KEY:
			default:
			{
//				addWhereClausePrimaryKey(sc, spec, spec.queryExp.filter, typeName, tbl, statement);
				whereGen.addWhereClausePrimaryKey(spec, spec.queryExp.filter, structType, selectFrag);
			}
				break;
			}
		}

		protected DStructType getMainType(QuerySpec spec) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
			return structType;
		}
		protected DStructType getMainType(String typeName) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
			return structType;
		}

		public String renderSelect(SelectStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
		public void setAliasCreator(AliasCreator aliasCreator) {
			this.aliasCreator = aliasCreator;
		}
	}