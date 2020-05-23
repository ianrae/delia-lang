package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.ConversionResult;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;


public class FragmentParserTestBase extends BDDBase {

	//---
	protected Delia delia;
	protected FactoryService factorySvc;
	protected DTypeRegistry registry;
	protected Runner runner;
	protected QueryBuilderService queryBuilderSvc;
	protected String sqlLine1;
	protected String sqlLine2;
	protected SqlStatementGroup currentGroup;
	protected List<Integer> numParamL = new ArrayList<>();
	protected String sqlLine3;
	protected String sqlLine4;
	protected String sqlLine5;

	protected DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}

	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}

	protected QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}


	protected List<TableInfo> createTblInfoL() {
//		TableExistenceServiceImpl.hackYesFlag = true;
		List<TableInfo> tblinfoL = new ArrayList<>();
//		TableInfo info = new  TableInfo("Address", "AddressCustomerDat1");
		TableInfo info = new  TableInfo("Customer", "CustomerAddressDat1");
		info.tbl2 = "Address";
		info.tbl1 = "Customer";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}
	protected List<TableInfo> createTblInfoLOtherWay() {
//		TableExistenceServiceImpl.hackYesFlag = true;
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Address", "AddressCustomerDat1");
		info.tbl2 = "Customer";
		info.tbl1 = "Address";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}

	protected void chkLine(int lineNum, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
		} else if (lineNum == 3) {
			assertEquals(expected, sqlLine3);
		} else if (lineNum == 4) {
			assertEquals(expected, sqlLine4);
		} else if (lineNum == 5) {
			assertEquals(expected, sqlLine5);
		}
	}
	protected void chkNoLine(int lineNum) {
		if (lineNum == 2) {
			assertEquals(null, sqlLine2);
		} else if (lineNum == 3) {
			assertEquals(null, sqlLine3);
		} else if (lineNum == 4) {
			assertEquals(null, sqlLine4);
		} else if (lineNum == 5) {
			assertEquals(null, sqlLine5);
		}
	}

	protected ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, null, sprigSvc);
		cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		cres.assocCrudMap = converter.getAssocCrudMap();
		return cres;
	}

	protected String convertToString(DValue dval) {
		if (dval.getType().isRelationShape()) {
			DRelation drel = (DRelation) dval;
			return drel.getForeignKey().asString();
		} else if (dval.getType().isStructShape()) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
			DValue inner = DValueHelper.getFieldValue(dval, pair.name);
			return inner.asString();
		} else {
			return dval.asString();
		}
	}
	protected void chkNumParams(Integer... args) {
		assertEquals("len", args.length, numParamL.size());
		int i = 0;
//		for(Integer k : numParamL) {
//			log.log("stat%d: %d", i, k);
//		}
		i = 0;
		for(Integer k : numParamL) {
			assertEquals(args[i++].intValue(), k.intValue());
		}
	}
	
	protected FragmentParserService createFragmentParserService(WhereFragmentGenerator whereGen, DeliaGenericDao dao, List<TableInfo> tblinfoL) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		if (tblinfoL == null) {
			tblinfoL = new ArrayList<>();		
		}
		DBAccessContext dbctx = new DBAccessContext(runner);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), dbctx, sqlHelperFactory, whereGen, null);
		return fpSvc;
	}

}
