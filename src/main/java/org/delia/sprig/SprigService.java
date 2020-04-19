package org.delia.sprig;

import java.util.Map;

import org.delia.type.DValue;

/**
 * Sprig is a well-known ruby library for seeding databases.
 * We implement synthetic_ids here in this service.
 * @author Ian Rae
 *
 */
public interface SprigService {

	void registerSyntheticId(String typeName, String syntheticIdName);
	boolean haveEnabledFor(String typeName);
	boolean haveEnabledFor(String typeName, String syntheticIdName);
	void setGeneratedId(String typeName, DValue idVal);
	DValue resolveSyntheticId(String typeName, String idValue);
	void rememberSynthId(String typeName, DValue dval, DValue generatedId, Map<String, DValue> extraMap);
}
