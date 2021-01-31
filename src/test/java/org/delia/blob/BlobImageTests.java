package org.delia.blob;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.delia.db.sizeof.DeliaTestBase;
import org.delia.runner.BlobLoader;
import org.delia.type.BlobType;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.InputStreamUtils;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Ian Rae
 *
 */
public class BlobImageTests extends DeliaTestBase { 
	
	@Test
	public void test() throws IOException {
		blobLoader = new BlobLoader();
		byte[] byteArr = loadPhoto();
		blobLoader.add("$A", new WrappedBlob(byteArr));
		
		String src = "let x = Flight[1]";
		execute(src);
		DValue dval = session.getFinalResult().getAsDValue();
		assertEquals(1, dval.asStruct().getField("field1").asInt());
		WrappedBlob wblob = dval.asStruct().getField("field2").asBlob();
		assertEquals(BlobType.BTYE_ARRAY, wblob.type());
		int n = byteArr.length;
		assertEquals(n, wblob.getByteArray().length);
		
		String path = dir + "out.png";
		OutputStream os = new FileOutputStream(path);
		os.write(wblob.getByteArray());
		os.close();
	}	
	

	//-------------------------
	private String dir = "src/main/resources/test/blob/";
	
	@Before
	public void init() {
	}

	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 int primaryKey, field2 blob } %s end", s);

		src += String.format("\n insert Flight {field1: 1, field2: '$A'}", s);
		return src;
	}

	private byte[] loadPhoto() throws IOException {
		String path = dir + "alberto.png";
		InputStream inputStream = new FileInputStream(path);
		try {
			byte[] bytes = InputStreamUtils.readAllBytesAndClose(inputStream);
			return bytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
