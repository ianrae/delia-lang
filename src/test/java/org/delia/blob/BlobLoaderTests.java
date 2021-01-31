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
		blobLoader.add("$A", new WrappedBlob(SMALL));
		
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
		blobLoader.add("$ZZZZZ", new WrappedBlob(SMALL));
		String src = "let x = Flight[1]";
		executeFail(src, "blob-loader-var-not-found");
	}	

	//-------------------------
	private boolean useSrc2;
	
	public static final byte[] SMALL = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20 };	
	public static final byte[] TEXTY = "Any String you want".getBytes();
	public static final byte[] BINARY1 = "\u00e0\u004f\u00d0\u0020\u00ea\u003a\u0069\u0010".getBytes();	
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		if (useSrc2) {
			return buildSrc2();
		}
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);

		src += String.format("\n insert Flight {field1: 1, field2: '$A'}", s);
//		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

	private String buildSrc2() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob optional } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: null }");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
		return src;
	}

}
