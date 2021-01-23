package org.delia.zdb;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.base.DBTestHelper;
import org.delia.base.UnitTestLogFactory;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.h2.H2ConnectionHelper;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.typebuilder.InternalTypeCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.zdb.h2.H2DeliaSessionCache;
import org.delia.zdb.h2.H2ZDBConnection;
import org.delia.zdb.h2.H2ZDBExecutor;
import org.delia.zdb.h2.H2ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.hls.HLSMemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class ZDBTests  extends BDDBase {

	@Test
	public void testMEM() {
		HLSMemZDBInterfaceFactory dbFactory = new HLSMemZDBInterfaceFactory(factorySvc);
		MemZDBExecutor dbexec = new MemZDBExecutor(factorySvc, dbFactory);
		dbexec.init1(registry);

		InternalTypeCreator typeCreator = new InternalTypeCreator();
		String typeName = "DELIA_ASSOC";
		DStructType datType = typeCreator.createDATType(registry, typeName);
		assertEquals(false, dbexec.doesTableExist(datType.getName()));
		dbexec.rawCreateTable(typeName);
		assertEquals(true, dbexec.doesTableExist(datType.getName()));

		DValue dval = createDatTableObj(datType, "dat1");

//		InsertContext ictx = new InsertContext();
//		ictx.extractGeneratedKeys = true;
//		ictx.genKeytype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//		DValue newDatIdValue = dbexec.executeInsert(dval, ictx);
//		assertEquals(1, newDatIdValue.asInt());
		dbexec.close();
	}

	@Test
	public void testH2() throws Exception {
		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
		H2ZDBInterfaceFactory dbFactory = new H2ZDBInterfaceFactory(factorySvc, connFact);
		
		H2ZDBConnection conn = (H2ZDBConnection) dbFactory.openConnection();
		ZDBExecutor dbexec = new H2ZDBExecutor(factorySvc, log, dbFactory, conn, new H2DeliaSessionCache());
		dbexec.init1(registry);

		InternalTypeCreator typeCreator = new InternalTypeCreator();
		String typeName = "DELIA_ASSOC";
		dbexec.deleteTable(typeName);
		
		DStructType datType = typeCreator.createDATType(registry, typeName);
		assertEquals(false, dbexec.rawTableDetect(datType.getName()));
		dbexec.rawCreateTable(typeName);
		assertEquals(true, dbexec.rawTableDetect(datType.getName()));

		DValue dval = createDatTableObj(datType, "dat1");

//		InsertContext ictx = new InsertContext();
//		ictx.extractGeneratedKeys = true;
//		ictx.genKeytype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//		ictx.actualDValForRawInsert = dval;
//		DValue newDatIdValue = dbexec.rawInsert(null, ictx); //null ok for MEM
//		assertEquals(1, newDatIdValue.asInt());
		dbexec.close();
	}

	// --
	private DeliaGenericDao dao;
	private Delia delia;
	private FactoryService factorySvc;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		this.dao = createDao();
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();

		this.session = delia.beginSession("");
		this.registry = session.getExecutionContext().registry;
	}

	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).log(new UnitTestLogFactory()).build();
		return new DeliaGenericDao(delia);
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		ZDBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}

	private DValue createDatTableObj(DStructType type, String datTableName) {
		StructValueBuilder structBuilder = new StructValueBuilder(type);

		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildString(datTableName);
		structBuilder.addField("tblName", dval);
		structBuilder.addField("leftName", dval); //not really correct
		structBuilder.addField("rightName", dval);

		boolean b = structBuilder.finish();
		if (! b) {
			return null;
		}
		dval = structBuilder.getDValue();
		return dval;
	}

}
