package org.delia.other;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SysDesignTests {
	
	public static class DataRow {
		private List<Object> columns = new ArrayList<>();

		public List<Object> getColumns() {
			return columns;
		}
		public void setColumns(List<Object> columns) {
			this.columns = columns;
		}
	}
	
	public interface DatabaseService {
		int getRowCount(String tableName);
		DataRow findById(String tableName, String id);
	}
	
	public static class OracleDatabaseService implements DatabaseService {
		@Override
		public int getRowCount(String tableName) {
			return 100;
		}

		@Override
		public DataRow findById(String tableName, String id) {
			DataRow row = new DataRow();
			row.getColumns().add(id);
			row.getColumns().add("other data");
			return row;
		}
	}
	
	public static class CustomerService {
		private DatabaseService databaseService;
		
		public String findCustomerData(String id) {
			DataRow row = databaseService.findById("Customers", id);
			return row.getColumns().get(1).toString();
		}

		public void setDatabaseService(DatabaseService databaseService) {
			this.databaseService = databaseService;
		}
	}

	
	@Test
	public void test() {
		CustomerService custService = new CustomerService();
		DatabaseService databaseService = new OracleDatabaseService();
//		DatabaseService databaseService = new MySqlDatabaseService();
		custService.setDatabaseService(databaseService);  //*** inject ***
		
		String s = custService.findCustomerData("1000");
		assertEquals("other data", s);
	}
}
