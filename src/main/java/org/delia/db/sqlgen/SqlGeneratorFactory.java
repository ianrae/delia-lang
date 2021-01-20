package org.delia.db.sqlgen;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.ConversionHelper;
import org.delia.type.DTypeRegistry;

public class SqlGeneratorFactory extends ServiceBase {
	
	private DTypeRegistry registry;
	
	public SqlGeneratorFactory(DTypeRegistry registry, FactoryService factorySvc) {
		super(factorySvc);
		this.registry = registry;
	}
	
	
	public SqlSelectStatement createSelect(DatIdMap datIdMap) {
		ConversionHelper conversionHelper = new ConversionHelper(registry, factorySvc);
		return new SqlSelectStatement(datIdMap, new SqlTableNameClause(), conversionHelper);
	}
	
	public SqlInsertStatement createInsert() {
		return new SqlInsertStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}
	
	public SqlMergeIntoStatement createMergeInto() {
		return new SqlMergeIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	public SqlMergeUsingStatement createMergeUsing() {
		return new SqlMergeUsingStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}
	
	
	
}
