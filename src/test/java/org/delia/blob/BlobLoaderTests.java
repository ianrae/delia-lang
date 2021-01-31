package org.delia.blob;

import static org.junit.Assert.assertEquals;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.runner.BlobLoader;
import org.delia.type.BlobType;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class BlobLoaderTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		blobLoader = new BlobLoader();
		blobLoader.add("$A", new WrappedBlob(BlobTests.SMALL));
		
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		WrappedBlob wblob = dval.asStruct().getField("field2").asBlob();
		assertEquals(BlobType.BTYE_ARRAY, wblob.type());
		String s = BlobUtils.toBase64(wblob.getByteArray());
		log.log(s);
		assertEquals("4E/QIA==", s);
		
		//check asString
		s = dval.asStruct().getField("field2").asString();
		assertEquals("4E/QIA==", s);
	}	
	
	@Test
	public void testNoLoader() {
		String src = "let x = Flight[1]";
		executeFail(src, "blob-loader-is-missing");
	}	
	
	@Test
	public void testBadLoaderVar() {
		blobLoader = new BlobLoader();
		blobLoader.add("$ZZZZZ", new WrappedBlob(BlobTests.SMALL));
		String src = "let x = Flight[1]";
		executeFail(src, "blob-loader-var-not-found");
	}	

	//-------------------------
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);

		src += String.format("\n insert Flight {field1: 1, field2: '$A'}", s);
//		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}
}
