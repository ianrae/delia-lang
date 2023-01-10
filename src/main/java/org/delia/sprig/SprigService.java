package org.delia.sprig;

import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DValue;

import java.util.Map;

/**
 * Sprig is a well-known ruby library for seeding databases.
 * We implement synthetic_ids here in this service.
 * @author Ian Rae
 *
 */
public interface SprigService {

	void registerSyntheticId(DTypeName typeName, String syntheticIdName);
	boolean haveEnabledFor(DTypeName typeName);
	boolean haveEnabledFor(DStructType structType);
	boolean haveEnabledFor(DTypeName typeName, String syntheticIdName);
	void setGeneratedId(DTypeName typeName, DValue idVal);
	DValue resolveSyntheticId(DStructType structType, String idValue);
	void rememberSynthId(DTypeName typeName, DValue dval, DValue generatedId, DValue synId);
}
