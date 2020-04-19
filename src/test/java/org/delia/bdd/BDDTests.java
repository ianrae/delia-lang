package org.delia.bdd;

import org.junit.Before;
import org.junit.Test;


public class BDDTests extends BDDTestBase {

	//let x = Actor  //no filter
	@Test
	public void testEmptyFilter()  {
		qhelper.chkQueryAll(TYPE0, 0);
		qhelper.chkQueryAll(TYPE1, 4);
	}

	//let x = Actor[10]  //by primary key (single key)
	@Test
	public void testSelectFilter()  {
		qhelper.chkQueryNotFound(TYPE0, 9);
		
		qhelper.chkQueryNotFound(TYPE1, 9);
		qhelper.chkQueryOne(TYPE1, 10);
		qhelper.chkQueryOne(TYPE1, 11);
		qhelper.chkQueryOne(TYPE1, 12);
		qhelper.chkQueryOne(TYPE1, 13);
	}
	
	//let x = Actor[id < 20]  //< op
	@Test
	public void testSelectFilterLT()  {
		qhelper.chkQueryOpLT(TYPE0, 100, 0);
		qhelper.chkQueryOpLT(TYPE0, -100, 0);
		
		qhelper.chkQueryOpLT(TYPE1, -100, 0);
		qhelper.chkQueryOpLT(TYPE1, 11, 1);
		qhelper.chkQueryOpLT(TYPE1, 21, 4);
		qhelper.chkQueryOpLT(TYPE1, 10, 0);
	}
	//let x = Actor[id <= 20]  //< op
	@Test
	public void testSelectFilterLE()  {
		qhelper.chkQueryOpLE(TYPE0, 100, 0);
		qhelper.chkQueryOpLE(TYPE0, -100, 0);
		
		qhelper.chkQueryOpLE(TYPE1, -100, 0);
		qhelper.chkQueryOpLE(TYPE1, 11, 2);
		qhelper.chkQueryOpLE(TYPE1, 21, 4);
		qhelper.chkQueryOpLE(TYPE1, 10, 1);
	}
	//let x = Actor[id > 20]  //< op
	@Test
	public void testSelectFilterGT()  {
		qhelper.chkQueryOpGT(TYPE0, 100, 0);
		qhelper.chkQueryOpGT(TYPE0, -100, 0);
		
		qhelper.chkQueryOpGT(TYPE1, -100, 4);
		qhelper.chkQueryOpGT(TYPE1, 11, 2);
		qhelper.chkQueryOpGT(TYPE1, 21, 0);
		qhelper.chkQueryOpGT(TYPE1, 10, 3);
	}
	//let x = Actor[id >= 20]  //< op
	@Test
	public void testSelectFilterGE()  {
		qhelper.chkQueryOpGE(TYPE0, 100, 0);
		qhelper.chkQueryOpGE(TYPE0, -100, 0);
		
		qhelper.chkQueryOpGE(TYPE1, -100, 4);
		qhelper.chkQueryOpGE(TYPE1, 11, 3);
		qhelper.chkQueryOpGE(TYPE1, 21, 0);
		qhelper.chkQueryOpGE(TYPE1, 10, 4);
	}
//	//let x = Actor[id < null]  //not allowed!
//	@Test(expected=DangException.class)
//	public void testSelectFilterLTError()  {
//		qhelper.chkQueryOpLTNull(TYPE0, 0);
//	}
	
	//WORKING ONE...
	@Test
	public void testWIP()  {
		qhelper.chkQueryOpLT(TYPE1, 11, 1);
	}
	
	@Before
	public void init() {
		super.init();
	}
}
