package org.delia.blob;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.delia.db.sizeof.DeliaTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * rules are built in RuleFuncFactory
 * @author Ian Rae
 *
 */
public class BlobTests extends DeliaTestBase { 
	
	public static class BlobUtils {
		//https://www.baeldung.com/java-base64-encode-and-decode
		public static String toBase64(byte[] byteArr) {
			String encodedString = Base64.getEncoder().encodeToString(byteArr);
			return encodedString;
		}
		public static byte[] fromBase64(String encodedString) {
			byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
			return decodedBytes;
		}
		
		public static InputStream toInputStream(byte[] byteArr) {
			InputStream is = new ByteArrayInputStream(byteArr);		
			return is;
		}
		
		public static byte[] hexStringToByteArray(String s) {
		    int len = s.length();
		    byte[] data = new byte[len / 2];
		    for (int i = 0; i < len; i += 2) {
		        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                             + Character.digit(s.charAt(i+1), 16));
		    }
		    return data;
		}		
		public static String byteArrayToHexString(byte[] byteArr) {
			StringBuilder sb = new StringBuilder();
			for(byte bb: byteArr) {
				String s = String.format("%x", bb);
				sb.append(s);
			}
			return sb.toString();
		}		
	}
	
	public static enum BlobType {
		FILE,
		BTYE_ARRAY
	}
	public static class WrappedBlob {
		
		private BlobType type;
		private byte[] byteArr;
		private Object file;

		public WrappedBlob(byte[] byteArr) {
			this.type = BlobType.BTYE_ARRAY;
			this.byteArr = byteArr;
		}
		public WrappedBlob(File f) {
			this.type = BlobType.FILE;
			this.file = f;
		}
		public BlobType type() {
			return type;
		}
		public byte[] getByteArray() {
			return byteArr;
		}
		public Object getFile() {
			return file;
		}
		
	}

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

	//-------------------------
	private boolean addSizeof = true;
	private String sizeofStr = "field2.sizeof(8)";
	private String expectedRuleFail = "rule-sizeof";
	
	private byte[] SMALL = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20 };	
	private byte[] TEXTY = "Any String you want".getBytes();
	private byte[] BINARY1 = "\u00e0\u004f\u00d0\u0020\u00ea\u003a\u0069\u0010".getBytes();	
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = addSizeof ? sizeofStr : "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 int } %s end", s);

		s =  "";
		src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
		src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		return src;
	}
	private void chkIt(String rule, String s, boolean b) {
		sizeofStr = rule;
		String src = String.format("insert Flight {field1: 3, field2: %s }", s);
		if (b) {
			execute(src);
		} else {
			executeFail(src, expectedRuleFail);
		}
	}


}
