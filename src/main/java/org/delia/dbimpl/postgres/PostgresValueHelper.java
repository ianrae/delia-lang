package org.delia.dbimpl.postgres;

import org.delia.core.FactoryService;
import org.delia.db.ValueHelper;
import org.delia.type.DValue;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgresValueHelper extends ValueHelper {

//	public PostgresValueHelper(FactoryService factorySvc, BlobCreator blobCreator) {
//		super(factorySvc, blobCreator);
//	}
	public PostgresValueHelper(FactoryService factorySvc) {
		super(factorySvc);
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
			
			//https://jdbc.postgresql.org/documentation/head/binary-data.html
			WrappedBlob wblob = dval.asBlob();
			InputStream stream = BlobUtils.toInputStream(wblob.getByteArray());
//			try {
//				log.log("%d fffffffffff: %d", index, stream.available());
//				stm.setBinaryStream(index++, stream, stream.available()ilable());
				stm.setBytes(index++, wblob.getByteArray());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			log.log("fffffffffff");
			
			//https://stackoverflow.com/questions/49110818/method-org-postgresql-jdbc-pgconnection-createclob-is-not-yet-implemented/52596666
//			WrappedBlob wblob = dval.asBlob();
//			Blob sqlBlob = blobCreator.createBlob();
//			sqlBlob.setBytes(0, wblob.getByteArray());
//			stm.setBlob(index++, sqlBlob);
		}
		break;
		default:
			index = super.doCreatePrepStatement(stm, dval, index);
			break;
		}
		return index;
	}
	
	
}