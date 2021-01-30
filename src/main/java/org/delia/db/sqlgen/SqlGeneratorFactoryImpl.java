package org.delia.db.sqlgen;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DTypeRegistry;

public class SqlGeneratorFactoryImpl extends ServiceBase implements SqlGeneratorFactory {
	
	protected DTypeRegistry registry;
	
	public SqlGeneratorFactoryImpl(DTypeRegistry registry, FactoryService factorySvc) {
		super(factorySvc);
		this.registry = registry;
	}
	
	
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createSelect(org.delia.assoc.DatIdMap)
	 */
	@Override
	public SqlSelectStatement createSelect(DatIdMap datIdMap) {
//		ConversionHelper conversionHelper = new ConversionHelper(registry, factorySvc);
		SqlWhereClause whereClause = new SqlWhereClause(registry, factorySvc);

		return new SqlSelectStatement(registry, factorySvc, datIdMap, new SqlTableNameClause(), whereClause);
	}
	
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createInsert()
	 */
	@Override
	public SqlInsertStatement createInsert() {
		return new SqlInsertStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createUpdate()
	 */
	@Override
	public SqlUpdateStatement createUpdate() {
		SqlWhereClause whereClause = new SqlWhereClause(registry, factorySvc);
		return new SqlUpdateStatement(new SqlTableNameClause(), new SqlValueListClause(), whereClause);
	}
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createDelete()
	 */
	@Override
	public SqlDeleteStatement createDelete() {
		SqlWhereClause whereClause = new SqlWhereClause(registry, factorySvc);
		return new SqlDeleteStatement(new SqlTableNameClause(), whereClause);
	}
	
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createMergeInto()
	 */
	@Override
	public SqlMergeIntoStatement createMergeInto() {
		return new SqlMergeIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	/* (non-Javadoc)
	 * @see org.delia.db.sqlgen.SqlGeneratorFactory#createMergeUsing()
	 */
	@Override
	public SqlMergeUsingStatement createMergeUsing() {
		return new SqlMergeUsingStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}


	@Override
	public void useDeleteIn(SqlDeleteStatement delStmt) {
		SqlWhereClause whereClause = new SqlWhereClause(registry, factorySvc);
		SqlDeleteInClause deleteInClause = new SqlDeleteInClause(whereClause);
		delStmt.useDeleteIn(deleteInClause);
	}


	@Override
	public SqlMergeAllIntoStatement createMergeAllInto() {
		return new SqlMergeAllIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	
	
	
}
