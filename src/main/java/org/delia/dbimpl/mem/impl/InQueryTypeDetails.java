package org.delia.dbimpl.mem.impl;

import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.List;

public class InQueryTypeDetails {
    public String field;
    public List<DValue> inValues = new ArrayList<>();
    public boolean allOp2AreValues;
}
