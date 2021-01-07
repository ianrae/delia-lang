package org.delia.db.newhls;


import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * New HLS. yet another attempt at a better replacement for exp objects.
 * -get rid of idea of spans!!
 *  
 * type and filter  Customer[id > 10]        initial type and WHERE filter
 *   filter:
 *     true or pkval [15]
 *     [not] val op val
 *     [not] val like str
 *     val in [sdfsdf]
 *     cond AND/OR cond
 *     date fns
 * throughChain    .addr.country             0 or more. contiguous chain
 * field           .name                     0 or 1. query one field, not an object
 * listFn          orderBy/distinct/limit/offset/first,last,ith      sort,paging. optional    
 * fetch           fks,fetch('aaa'),...          load 0 or more sub-objects.
 * calcFn          exists,count,min,max,average,sum,...   query produces a calculated result. 
 * 
 * goals
 *  -convert filter to better objects
 *  -MEM
 *  -H2,PG using SQL 
 *    -joins caused by throughChain,fetch
 *      -implicit join: orderBy, field, and fields mentioned in filter    
 *        -only join pk if parent (in 1:1 or 1:N)
 *  -use newHLS for filters in update/upsert/delete statements
 *    
 * TODO: add delia inject attack prevention tests!
 * 
 * steps
 *  -build filtercond
 *  -build hld
 *  -fill in fieldVal.structField on all SYMBOLS that are fieldnames (they can be a let var or a fieldname. fieldname takes precedence)
 *   -actually resolve varnames to scalar values here.
 *  -build joinL and then aliases
 *  -build fieldL. affected by joins, fetch,fks,count(*), ....
 *    field should have structField.
 *    fields grouped in columnRuns (use a string groupName)
 *	public boolean isAssocField; and probably the alias name b.custId as addr
 *     -we don't want to build or construct anything during query execution. 
 *     all should be in field so that we can cache it.
 *  -should handle scalar results (count() or .firstName)
 *  -should handle select * query (lookup fields by name in rs)    
 * -now we have a high level version of the query in hld.
 * -generate sql. types of sql  
 *   -select *
 *   -count
 *   -regular
 * Development plan
 * -do Customer[true] in MEM and sql (don't actually wire up h2)
 * -do [45]
 * -do [id > 10] //leave in and like for later
 *  -do not, and do bool,int,long,number,date,enum
 * -do order/limit stuff
 * -do .firstName scalar result
 * -do simple join, 1:1, 1:N, M:N
 * -do fetch join, then implicit joins
 * -do through join, and self-join
 * -do first,last,ith,count,...
 * 
 * -idea is a new set of unit tests that fully test MEM and sql generation
 * 
 * @author Ian Rae
 *
 */
public class HLDBasicTests extends NewHLSTestBase {
	
	@Test
	public void testHLDField() {
		String src = "let x = Flight[15]";
		HLDQuery hld = buildFromSrc(src, 0); 
		chkRawSql(hld, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=15");
		chkFullSql(hld, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=?");
	}	

	


	//-------------------------
	private String pkType = "int";
	private boolean addOrderDate = false;
	private HLDManager mgr;

	@Before
	public void init() {
		//createDao();
	}

	@Override
	protected String buildSrc() {
		String s = addOrderDate ? ", orderDate date" : "";
		String src = String.format("type Flight struct {field1 %s primaryKey, field2 int %s } end", pkType, s);

		s = addOrderDate ? ", orderDate: '2019'" : "";
		if (pkType.equals("string")) {
			src += String.format("\n insert Flight {field1: 'ab', field2: 10 %s}", s);
			src += String.format("\n insert Flight {field1: 'cd', field2: 20 %s}", s);

		} else {
			src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
			src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		}
		return src;
	}
	private void chkStm(SqlStatement stm, String expected, String... args) {
		log.log(stm.sql);
		assertEquals(expected, stm.sql);
		assertEquals(args.length, stm.paramL.size());
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			DValue dval = stm.paramL.get(i);
			assertEquals(arg, dval.asString());
		}
	}
	private void chkRawSql(HLDQuery hld, String expected) {
		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void chkFullSql(HLDQuery hld, String expected) {
		SqlStatement stm = mgr.generateSql(hld);
		chkStm(stm, expected, "15");
	}



	private HLDQuery buildFromSrc(String src, int expectedJoins) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		
		mgr = new HLDManager(this.session.getExecutionContext().registry, delia.getFactoryService());
		HLDQuery hld = mgr.fullBuildQuery(queryExp);
		log.log(hld.toString());
		assertEquals(expectedJoins, hld.joinL.size());
		return hld;
	}


}
