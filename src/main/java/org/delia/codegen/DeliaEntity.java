package org.delia.codegen;

import java.util.Map;

public interface DeliaEntity {
	Map<String,Object> internalSetValueMap();
	//resetUnchangedFields();
}