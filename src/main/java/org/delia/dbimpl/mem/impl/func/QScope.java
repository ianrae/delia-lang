package org.delia.dbimpl.mem.impl.func;

import org.delia.lld.LLD;
import org.delia.type.DStructType;

import java.util.ArrayList;
import java.util.List;

//each scope has at most one structField and one finalScalarField
public class QScope {
    public DStructType structType;
    public List<LLD.LLEx> funcL = new ArrayList<>(); //only functions
    public List<LLD.LLEx> scalarFuncL = new ArrayList<>(); //only functions. min,max,avg
    public LLD.LLFinalFieldEx structField; //eg .addr
    public LLD.LLFinalFieldEx finalScalarField; //eg .wid
}
