package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;

import org.delia.hld.JoinElement;
import org.delia.runner.FetchRunner;

/**
 * 
 * @author Ian Rae
 *
 */
public class ImplicitFetchContext { 
	public List<JoinElement> implicitFetchL = new ArrayList<>(); //used by MEM only
	public FetchRunner fetchRunner;
}
