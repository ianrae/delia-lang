package org.delia.db.sql.fragment;

public interface SqlFragment {
	String render();
	int getNumSqlParams(); //number of ? params
}