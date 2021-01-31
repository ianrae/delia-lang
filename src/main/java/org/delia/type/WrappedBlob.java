package org.delia.type;

import java.io.File;

public class WrappedBlob {
	
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