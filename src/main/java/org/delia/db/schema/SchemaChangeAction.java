package org.delia.db.schema;

import java.util.*;

/**
 * A schema change that involves several fields, such as an index.
 * @author ian
 *
 */
public class SchemaChangeAction {
	public String typeName;
	public String changeType; //+UFC or -UFC
	public List<String> paramsL = new ArrayList<>();
}
