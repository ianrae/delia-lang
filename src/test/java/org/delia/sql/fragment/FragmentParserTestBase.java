package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.memdb.MemDBInterface;
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
import org.junit.Before;


public class FragmentParserTestBase extends NewBDDBase {



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

	protected DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	protected QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}


	protected List<TableInfo> createTblInfoL() {
		TableExistenceServiceImpl.hackYesFlag = true;
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Address", "AddressCustomerAssoc");
		info.tbl1 = "Address";
		info.tbl2 = "Customer";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}
	protected List<TableInfo> createTblInfoLOtherWay() {
		TableExistenceServiceImpl.hackYesFlag = true;
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Customer", "CustomerAddressAssoc");
		info.tbl1 = "Customer";
		info.tbl2 = "Address";
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
		for(Integer k : numParamL) {
			assertEquals(args[i++].intValue(), k.intValue());
		}
	}
}
