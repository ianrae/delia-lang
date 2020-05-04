package org.delia.bdd.core;

import java.util.ArrayList;
import java.util.List;

public class BDDTest {
	public BDDFeature feature;
	public String title;
	public String expectedType;
	public boolean expectDVal = true;
	public boolean skip = false;
	public List<String> givenL = new ArrayList<>();
	public List<String> whenL = new ArrayList<>();
	public List<String> thenL = new ArrayList<>();
	public boolean chainNextTest;
	public boolean allowSemiColons;
	public boolean useSafeMigrationPolicy = true;
	public String cleanTables;
}