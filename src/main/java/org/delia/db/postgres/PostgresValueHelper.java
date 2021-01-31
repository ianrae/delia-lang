package org.delia.db.postgres;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.delia.core.FactoryService;
import org.delia.db.ValueHelper;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;
import org.delia.zdb.BlobCreator;

public class PostgresValueHelper extends ValueHelper {

	public PostgresValueHelper(FactoryService factorySvc, BlobCreator blobCreator) {
		super(factorySvc, blobCreator);
	}
	
	@Override
	protected int doCreatePrepStatement(PreparedStatement stm, DValue dval, int index) throws SQLException {
		if (dval == null) {
			stm.setObject(index++, null);
			return index;
		}

		switch(dval.getType().getShape()) {
		case BLOB:
		{
//			//h2 and postgres both use hex format
//			String base64Str = dval.asString();
//			String hex = BlobUtils.base64ToHexString(base64Str);
//			
//			String s = String.format("decode('%s', 'hex')", hex);
//			stm.setString(index++, s);
			
			WrappedBlob wblob = dval.asBlob();
			InputStream stream = BlobUtils.toInputStream(wblob.getByteArray());
			stm.setBlob(index++, stream); 
		}
		break;
		default:
			super.doCreatePrepStatement(stm, dval, index);
			break;
		}
		return index;
	}
	
	
}