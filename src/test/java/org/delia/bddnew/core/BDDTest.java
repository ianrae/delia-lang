package org.delia.bddnew.core;

import java.util.ArrayList;
import java.util.List;

public class BDDTest {
    public String title;
    public boolean chainNextTest;
    public boolean skip = false;
    public boolean allowSemiColons = false;
    public String expectedType; //thenType
    public boolean expectDValFlag;
    public List<BDDSnippet> givenL = new ArrayList<>();
    public List<BDDSnippet> thenL = new ArrayList<>();
    public List<BDDSnippet> whenL = new ArrayList<>();
    public boolean bulkInsertEnabled;
}
