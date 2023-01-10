package org.delia.ast;

import org.delia.compiler.ast.Exp;
import org.delia.hld.HLD;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.util.DRuleHelper;
import org.delia.compiler.BuildCallback;

import java.util.List;

public class MyCallback implements BuildCallback {
    private final boolean isReversed;
    SyntheticDatService datSvc;

    public MyCallback(SyntheticDatService datSvc, boolean isReversed) {
        this.datSvc = datSvc;
        this.isReversed = isReversed;
    }

    @Override
    public void doCallback(List<HLD.HLDStatement> hldStatements, DTypeRegistry registry) {
        if (isReversed) {
            System.out.println("callback building AddressCustomerDat1!");
            Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Address"), new DTypeName(null, "Customer"), "cust");
            joinInfo.leftType = (DStructType) registry.getType(joinInfo.leftTypeName);
            joinInfo.rightType = (DStructType) registry.getType(joinInfo.rightTypeName);
            if (joinInfo.leftType != null && joinInfo.rightType != null) {
                joinInfo.relinfo = DRuleHelper.findMatchingRuleInfo(joinInfo.leftType, joinInfo.throughField);
                datSvc.buildAssoc(joinInfo, "id");
            }
            return;
        }
        System.out.println("callback building CustomerAddressDat1!");
        Exp.JoinInfo joinInfo = new Exp.JoinInfo(new DTypeName(null, "Customer"), new DTypeName(null, "Address"), "addr");
        joinInfo.leftType = (DStructType) registry.getType(joinInfo.leftTypeName);
        joinInfo.rightType = (DStructType) registry.getType(joinInfo.rightTypeName);
        if (joinInfo.leftType != null && joinInfo.rightType != null) {
            joinInfo.relinfo = DRuleHelper.findMatchingRuleInfo(joinInfo.leftType, joinInfo.throughField);
            datSvc.buildAssoc(joinInfo, "id");
        }
    }
}
