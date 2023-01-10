package org.delia.bddnew.core;

import org.delia.DeliaSession;
import org.delia.error.DeliaError;
import org.delia.runner.ResultValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BDDSnippetResult {
    public boolean ok;
    public List<DeliaError> errors = new ArrayList<>();
    public ResultValue resValue; //if snippet is delia
    public DeliaSession sess;
    public Map<String,String> nameHintMap = new HashMap<>(); //Address.cust=>addr
}
