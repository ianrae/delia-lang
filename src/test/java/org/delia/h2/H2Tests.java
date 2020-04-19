package org.delia.h2;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.delia.base.UnitTestLog;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.InstrumentedDBInterface;
import org.delia.db.h2.H2DBConnection;
import org.delia.db.h2.H2ErrorConverter;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.CompilerHelper;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.junit.Before;
import org.junit.Test;

public class H2Tests {

	@Test
	public void test() throws Exception {
		//        assertEquals(1,2);
		openDB();
	}

	@Test
	public void test2() throws Exception {
		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
		H2DBConnection conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
		log.log("here we go..");
		conn.openDB();
		
		log.log("and..");
		conn.executeRawSql("DROP TABLE IF EXISTS cars;");
		conn.executeRawSql("CREATE TABLE cars(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");

		conn.executeRawSql("INSERT INTO cars(name, price) VALUES('Audi', 52642);");
		conn.executeRawSql("INSERT INTO cars(name, price) VALUES('Mercedes', 57127);");       

		log.log("and query..");
		ResultSet rs = conn.execRawQuery("SELECT * from cars;");
		if (rs.next()) {
			System.out.println(rs.getInt(1));
			System.out.println(rs.getString(2));
			System.out.println(rs.getInt(3));
		}        
		
		dumpSchema(conn);

		conn.close();
		log.log("end.");
	}
	
	private void dumpSchema(H2DBConnection conn) throws SQLException {
		log.log("dump schema: TABLES...");
		ResultSet rs = conn.execRawQuery("SELECT * from information_schema.tables where TABLE_SCHEMA='PUBLIC';");
		while (rs.next()) {
			System.out.println(rs.getString("TABLE_NAME"));
		}        
		log.log("CARS columns...");

		rs = conn.execRawQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS where table_name = 'CARS';");
		while (rs.next()) {
			String s1 = rs.getString("COLUMN_NAME");
			Boolean b2 = rs.getBoolean("IS_NULLABLE");
			String s3 = rs.getString("DATA_TYPE");
			String s4 = rs.getString("TYPE_NAME");
			System.out.println(String.format("%s %b %s %s", s1, b2, s3, s4));
		}        
				
				
	}

	@Test
	public void testDetectTblManual() throws Exception {
		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
		H2DBConnection conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
		log.log("here we gox..");
		conn.openDB();
		
		log.log("and..");
		conn.executeRawSql("DROP TABLE IF EXISTS cars;");
		conn.executeRawSql("CREATE TABLE cars(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");

		log.log("and query..");
		ResultSet rs = conn.execRawQuery("SELECT count(*) from cars;");
		if (rs.next()) {
			System.out.println(rs.getInt(1));
		}        
		
//		log.log("and query Airport..");
//		rs = conn.execQuery("SELECT count(*) from Airport;");

		conn.close();
		log.log("end.");
	}
	
	@Test
	public void testDetectTbl() throws Exception {
		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
		H2DBConnection conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
		log.log("here we gox..");
		conn.openDB();
		
		log.log("and..");
		conn.executeRawSql("DROP TABLE IF EXISTS cars;");
		conn.executeRawSql("CREATE TABLE cars(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");

		log.log("and query..");
		boolean exists = conn.execTableDetect("cars");
		assertEquals(true, exists);
		
//		log.log("and query Airport..");
//		exists = conn.execTableDetect("Airport");
//		assertEquals(false, exists);

		conn.close();
		log.log("end.");
	}
	
	//--
	protected InstrumentedDBInterface dbInterface;
	protected RunnerHelper helper = new RunnerHelper();
	protected Runner runner;
	protected Log log = new UnitTestLog();
	protected CompilerHelper chelper = new CompilerHelper(null, log);
	protected ErrorTracker et = new SimpleErrorTracker(log);
	protected FactoryService factorySvc = new FactoryServiceImpl(log, et);

	@Before
	public void init() {
		
	}
	private void openDB() throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		// add application code here
		log.log("here we go..");
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery("SELECT 1+1");
		if (rs.next()) {
			System.out.println(rs.getInt(1));
		}        

		log.log("and..");
		stm = conn.createStatement();
		boolean b = stm.execute("DROP TABLE IF EXISTS cars;");
		b = stm.execute("CREATE TABLE cars(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");
		//       INSERT INTO cars(name, price) VALUES('Audi', 52642);
		//       INSERT INTO cars(name, price) VALUES('Mercedes', 57127);     
		int x = stm.getUpdateCount();
		log.log(String.format("b:%b %d", b, x));

		b = stm.execute("INSERT INTO cars(name, price) VALUES('Audi', 52642);");
		x = stm.getUpdateCount();
		log.log(String.format("xb:%b %d", b, x));
		b = stm.execute("INSERT INTO cars(name, price) VALUES('Mercedes', 57127);");       
		log.log(String.format("b:%b", b));

		log.log("and query..");
		stm = conn.createStatement();
		rs = stm.executeQuery("SELECT * from cars;");
		if (rs.next()) {
			System.out.println(rs.getInt(1));
			System.out.println(rs.getString(2));
			System.out.println(rs.getInt(3));
		}        

		conn.close();
		log.log("end.");
	}    

	private String fix(String jsonstr) {
		return jsonstr.replace("'", "\"");
	}
}
