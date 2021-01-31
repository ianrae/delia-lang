package org.delia.zdb.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.delia.core.FactoryService;
import org.delia.db.DBErrorConverter;
import org.delia.db.ResultSetHelper;
import org.delia.db.ValueHelper;
import org.delia.db.postgres.PostgresValueHelper;
import org.delia.db.sql.ConnectionFactory;
import org.delia.zdb.h2.H2DBConnection;

public class PostgresDBConnection extends H2DBConnection {

	public PostgresDBConnection(FactoryService factorySvc, ConnectionFactory connectionFactory, DBErrorConverter errorConverter) {
		super(factorySvc, connectionFactory, errorConverter);
		this.valueHelper = new PostgresValueHelper(factorySvc, this);
	}

	@Override
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType, boolean useFieldName) {
		ResultSet rs = null;
		try {
			log.log("SQL: %s", sql);
			Statement stm = conn.createStatement();
			rs = stm.executeQuery(sql);
			if (rs != null) {
				int n = ResultSetHelper.getColumnCount(rs);
				//so we want to enumerate and capture
				int iConstrainName = 3;
				int iConstrainType = 7;
				int iTable = 6;
//				int iColumn = 10;

				while(rs.next()) {
					String cname = getRsValue(rs, iConstrainName);
					String ctype= getRsValue(rs, iConstrainType);
					String tbl = getRsValue(rs, iTable);
//					String field = getRsValue(rs, iColumn);

					//for now assume only one
					//TODO this needs to be more robust. some constraints have names like:
					//customer_height_key  or 2200_402813_1_not_null
					if (useFieldName) {
						//not supported for postgres
					} else {
						if (tableName.equalsIgnoreCase(tbl) && cname.toLowerCase().contains(fieldName)) {
							if (constraintType.equalsIgnoreCase(ctype)) {
								return cname;
							}
						}
					}
					
					
				}
			}        
		} catch (SQLException e) {
			convertAndRethrowException(e);
		}

		return null;
	}

	@Override
	public ValueHelper createValueHelper() {
		return new PostgresValueHelper(factorySvc, this);
	}
}