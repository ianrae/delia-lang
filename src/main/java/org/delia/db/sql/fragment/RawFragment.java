package org.delia.db.sql.fragment;


/**
 * A raw piece of sql.
 * 
 * Be sure to follow rules about using aliases or not 
 * manually.
 * 
 * @author Ian Rae
 *
 */
public class RawFragment implements SqlFragment {
	public String rawSql;
	public int numSqlParams = 0; //!!
	
	public RawFragment(String rawSql) {
		this.rawSql = rawSql;
	}
	
	@Override
	public String render() {
		return rawSql;
	}
	
	@Override
	public int getNumSqlParams() {
		return numSqlParams;
	}
}