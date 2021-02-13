package org.delia.app;


import static org.junit.Assert.assertEquals;

import org.delia.Delia;
import org.delia.base.DBTestHelper;
import org.junit.Before;
import org.junit.Test;


public class LambdaTests extends DaoTestBase {
	
	public interface DoSomething {
		void doSomething(String s);
	}
	public interface DoSomething2 {
		String doSomething(String s);
	}
	public interface DoSomething3<T> {
		T doSomething();
	}
	public static class MySvc {
		<T> T foo(DoSomething3<T> some3) {
			return some3.doSomething();
		}
	}
	public interface TransactionProvider {
		void startTransaction();
		void commitTransaction();
		void rollbackTransaction();
	}
	public static class DoNothingTransactionProvider implements TransactionProvider {

		@Override
		public void startTransaction() {
			System.out.println("start..");
		}
		@Override
		public void commitTransaction() {
			System.out.println("commit");
		}
		@Override
		public void rollbackTransaction() {
			System.out.println("rollback!");
		}
	}
	public static class MyDeliaSvc {
		public TransactionProvider transProvider = new DoNothingTransactionProvider();
		<T> T foo(DoSomething3<T> some3) {
			transProvider.startTransaction();
			T res = null;
			try {
				res = some3.doSomething();
				transProvider.commitTransaction();
			} catch(Exception e) {
				transProvider.rollbackTransaction();
				throw e;
			}
			return res;
		}
	}
	
	
	
	@Test
	public void test() {
		String s2 = "pre";
		String s3 = null;
		DoSomething myInterface = (String text) -> {
		    System.out.println(s2 + text);
		};		
		
		log.log("ok");
		myInterface.doSomething("abc");
	}

	@Test
	public void test2() {
		String s2 = "pre";
		String s3 = null;
		DoSomething2 myInterface = (String text) -> {
		    System.out.println(s2 + text);
		    return s2 + text;
		};		
		
		log.log("ok");
		s3 = myInterface.doSomething("abc");
		log.log("s3: %s", s3);
	}
	
	@Test
	public void test3() {
		String s2 = "pre";
		String s3 = null;
		DoSomething3<String> myInterface = () -> {
		    System.out.println(s2 + "A");
		    return s2 + "A";
		};		
		
		log.log("ok");
		s3 = myInterface.doSomething();
		log.log("s3: %s", s3);
	}
	@Test
	public void test3a() {
		String s2 = "pre";
		MySvc svc = new MySvc();
		
		String s3 = svc.foo(() -> {
		    System.out.println(s2 + "A");
		    return s2 + "A";
		});		
		
		log.log("s3: %s", s3);
	}
	@Test
	public void test4() {
		String s2 = "pre";
		MyDeliaSvc svc = new MyDeliaSvc();
		
		String s3 = svc.foo(() -> {
		    System.out.println(s2 + "A");
		    return s2 + "A";
		});		
		
		log.log("s3: %s", s3);
	}
	@Test(expected=RuntimeException.class)
	public void test4Fail() {
		String s2 = "pre";
		MyDeliaSvc svc = new MyDeliaSvc();
		
		String s3 = svc.foo(() -> {
		    System.out.println(s2 + "A");
		    throw new RuntimeException("oops");
		});		
		
		log.log("s3: %s", s3);
	}
	
	
	//---

	@Before
	public void init() {
	}

	private TypeDao createDao(String typeName) {
		Delia delia = DBTestHelper.createNewDelia();
		return new TypeDao(typeName, delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
}
