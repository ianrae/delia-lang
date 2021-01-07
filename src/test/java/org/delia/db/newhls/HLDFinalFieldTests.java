package org.delia.db.newhls;


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
 *  -do not, and do bool,int,long,number,date
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
public class HLDFinalFieldTests extends NewHLSTestBase {
	@Test
	public void testScalar() {
		useCustomer11Src = true;
		String src = "let x = Customer[55].x";
		
		HLDQuery hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.x FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void testRelationFieldParent() {
		useCustomer11Src = true;
		String src = "let x = Customer[55].addr";
		
		HLDQuery hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.x FROM Customer as t0 WHERE t0.cid=?", "55");
	}
//	@Test
//	public void testFKS11Child() {
//		useCustomer11Src = true;
//		String src = "let x = Address[100].fks()";
//		
//		HLDQuery hld = buildFromSrc(src, 0); 
//		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust FROM Address as t0 WHERE t0.id=?", "100");
//	}	
//	
//	@Test
//	public void testFetch11Parent() {
//		useCustomer11Src = true;
//		String src = "let x = Customer[55].fetch('addr')";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t1.y,t1.cust FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=?", "55");
//	}	
//	@Test
//	public void testFetch11Child() {
//		useCustomer11Src = true;
//		String src = "let x = Address[100].fetch('cust')";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust,t1.cid,t1.x FROM Address as t0 JOIN Customer as t1 ON t0.cust=t1.cid WHERE t0.id=?", "100");
//	}	
//	
//	//TODO Customer.addr1 and .addr2 and try fetching one or both
//	//two addr
//	@Test
//	public void testFKS11Parent2Addr() {
//		use11TwoAddr = true;
//		String src = "let x = Customer[55].fks()";
//		
//		HLDQuery hld = buildFromSrc(src, 2); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t2.id FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 JOIN Address as t2 ON t0.cid=t2.cust2 WHERE t0.cid=?", "55");
//	}	
//	@Test
//	public void testFKS11Child2Addr() {
//		use11TwoAddr = true;
//		String src = "let x = Address[100].fks()";
//		
//		HLDQuery hld = buildFromSrc(src, 0); 
//		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust1,t0.cust2 FROM Address as t0 WHERE t0.id=?", "100");
//	}	
//	
//	@Test
//	public void testFetch11Parent2Addr() {
//		use11TwoAddr = true;
//		String src = "let x = Customer[55].fetch('addr1')";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t1.y,t1.cust1,t1.cust2 FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 WHERE t0.cid=?", "55");
//	}
//	@Test
//	public void testFetch11Parent2AddrB() {
//		use11TwoAddr = true;
//		String src = "let x = Customer[55].fetch('addr1', 'addr2')";
//		
//		HLDQuery hld = buildFromSrc(src, 2); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t1.y,t1.cust1,t1.cust2,t2.id,t2.y,t2.cust1,t2.cust2 FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 JOIN Address as t2 ON t0.cid=t2.cust2 WHERE t0.cid=?", "55");
//	}
//	
//	@Test
//	public void testFetch11Child2Addr() {
//		use11TwoAddr = true;
//		String src = "let x = Address[100].fetch('cust1')";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust1,t0.cust2,t1.cid,t1.x FROM Address as t0 JOIN Customer as t1 ON t0.cust1=t1.cid WHERE t0.id=?", "100");
//	}	
//
//	//implicit fetch
//	@Test
//	public void testImplicitOrderBy() {
//		useCustomer11Src = true;
//		String src = "let x = Customer[55].orderBy('addr')";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=? ORDER BY t1.addr", "55");
//	}
//	@Test
//	public void testImplicitFilter() {
//		useCustomer11Src = true;
//		String src = "let x = Customer[addr.y == 55]";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t1.y == ?", "55");
//	}
//	//TODO then do let x = Customer[addr.y == 55].orderBy('addr') and ensure not two joins!
	
	//-------------------------
	private boolean use11TwoAddr;
	
	@Override
	protected String buildSrc() {
		if (use11TwoAddr) {
			String src = " type Customer struct {cid int unique, x int, relation addr1 Address 'a1' one optional parent, relation addr2 Address 'a2' one optional parent } end";
			src += "\n type Address struct {id int unique, y int, relation cust1 Customer 'a1' one optional, relation cust2 Customer 'a2' one optional } end";
			return src;
		} else {
			return super.buildSrc();
		}
	}


}
