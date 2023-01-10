package org.delia.sql;

import org.delia.db.ResultSetHelper;
import org.delia.db.ValueHelper;
import org.delia.log.DeliaLog;
import org.delia.type.DValue;
import org.delia.type.TypePair;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetWrapper {
	private ResultSet rs;
	private ValueHelper valueHelper;
	private boolean logResultSetDetails;
	private DeliaLog log;
	
	public ResultSetWrapper(ResultSet rs, ValueHelper valueHelper, boolean logResultSetDetails, DeliaLog log) {
		this.rs = rs;
		this.valueHelper = valueHelper;
		this.logResultSetDetails = logResultSetDetails;
		this.log = log;
	}
	
	public boolean next() throws SQLException {
		boolean b = rs.next();
		if (logResultSetDetails) {
			log.log("RSW: --- row %b ---", b);
		}
		return b;
	}
	
	public String getString(String columnLabel) throws SQLException {
		String s = rs.getString(columnLabel);
		if (logResultSetDetails) {
			log.log("bRSW: str %s", s);
		}
		if (rs.wasNull()) {
			return null;
		}
		return s;
	}
	public String getString(int columnIndex) throws SQLException {
		String s = rs.getString(columnIndex);
		if (logResultSetDetails) {
			log.log("cRSW: row %s", s);
		}
		if (rs.wasNull()) {
			return null;
		}
		return s;
	}
	
	public boolean hasColumn(String colName) throws SQLException {
		return ResultSetHelper.hasColumn(rs, colName);
	}
	
	public DValue readField(TypePair pair, DBAccessContext dbctx) throws SQLException {
		DValue dval = valueHelper.readField(pair, rs, dbctx);
		if (logResultSetDetails) {
			log.log("dRSW: field %s", dval == null ? "null" : dval.asString());
		}
		
		return dval;
	}
	
	public DValue readFieldByColumnIndex(TypePair pair, int index, DBAccessContext dbctx) throws SQLException {
		DValue dval = valueHelper.readFieldByColumnIndex(pair, rs, index, dbctx);
		if (logResultSetDetails) {
			log.log("eRSW: field %s", dval == null ? "null" : dval.asString());
		}
		
		return dval;
	}
	
}
