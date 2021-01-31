package org.delia.blob;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.type.BlobType;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class BlobTests extends DeliaTestBase { 
	
	@Test
	public void test() {
		String s = BlobUtils.toBase64(SMALL);
		log.log(s);
	}	

	@Test
	public void testHex() {
		String hex = "41424e";
		byte[] ar = BlobUtils.hexStringToByteArray(hex);
		
		String hex2 = BlobUtils.byteArrayToHexString(ar);
		assertEquals(hex2, hex);
	}	
	
	@Test
	public void testStreamFixedSize() throws IOException {
		InputStream stream = BlobUtils.toInputStream(BINARY1);
		assertEquals(8, stream.available());
		
		byte[] targetArray = new byte[stream.available()];
	    stream.read(targetArray);
	    String s = BlobUtils.byteArrayToHexString(targetArray);
	    log.log(s);
	    assertEquals("e04fd020ea3a6910", s);
	}	
	
	@Test
	public void testStreamUnknownSize() throws IOException {
		InputStream stream = BlobUtils.toInputStream(BINARY1);
		
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int nRead;
	    byte[] data = new byte[1024];
	    while ((nRead = stream.read(data, 0, data.length)) != -1) {
	        buffer.write(data, 0, nRead);
	    }

	    buffer.flush();
	    byte[] byteArray = buffer.toByteArray();		
	    String s = BlobUtils.byteArrayToHexString(byteArray);
	    log.log(s);
	    assertEquals("e04fd020ea3a6910", s);
	}	
	
	@Test
	public void testDelia() {
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
	public void testDeliaNull() {
		useSrc2 = true;
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		DValue inner = dval.asStruct().getField("field2");
		assertEquals(null, inner);
	}	

	//-------------------------
	private boolean useSrc2;
	
	private byte[] SMALL = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20 };	
	private byte[] TEXTY = "Any String you want".getBytes();
	private byte[] BINARY1 = "\u00e0\u004f\u00d0\u0020\u00ea\u003a\u0069\u0010".getBytes();	
	
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

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: '4E/QIA=='}");
		src += String.format("\n insert Flight {field1: 2, field2: '4E/QIA=='}");
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
