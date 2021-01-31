package org.delia.runner;
import java.util.*;

import org.delia.type.WrappedBlob;

/**
 * Blobs are large, so it is too expensive to render them to base64 in order
 * to insert/update/delete them.
 * 
 * A BlobLoader contains byte arrays in a map.
 * Put the byte array in the blob loader with a name, such as "$foo". 
 * 
 * Names MUST begin with "$".
 * 
 * Then reference the name in the delia statement:
 *   insert Customer { id: 1, photo:"$foo" }
 * 
 * 
 * @author ian
 *
 */
public class BlobLoader {
	private Map<String,WrappedBlob> map = new HashMap<>();
	
	public void add(String name, WrappedBlob wblob) {
		map.put(name, wblob);
	}
	
	public byte[] getByteArray(String name) {
		WrappedBlob wblob = map.get(name);
		if (wblob == null) return null;
		return wblob.getByteArray();
	}

	public Map<String, WrappedBlob> getMap() {
		return map;
	}
}
