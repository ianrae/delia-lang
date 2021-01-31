package org.delia.db.postgres;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.delia.core.FactoryService;
import org.delia.db.ValueHelper;
import org.delia.type.DValue;
import org.delia.util.BlobUtils;

public class PostgresValueHelper extends ValueHelper {

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
			//h2 and postgres both use hex format
			String base64Str = dval.asString();
			String hex = BlobUtils.base64ToHexString(base64Str);
			stm.setString(index++, "RRRR" + hex);
			//TODO: use stm.setBlob later
		}
		break;
		default:
			super.doCreatePrepStatement(stm, dval, index);
			break;
		}
		return index;
	}
	
	
}