//package org.delia.dbimpl.mem.impl.filter;
//
//import org.delia.tok.Tok;
//import org.delia.type.DValue;
//
//public class LongOpEvaluator extends OpEvaluatorBase {
//
//    public LongOpEvaluator(OP op, String fieldName) {
//        super(op, fieldName);
//    }
//
//    @Override
//    protected boolean doMatch(Object left) {
//        DValue dval = (DValue) left;
//        Boolean b = checkNull(dval, rightVar);
//        if (b != null) {
//            return b;
//        }
//        Long n1 = resolveToLong(dval);
//        Long n2;
//        //auto-promote int values
//        n2 = ((Tok.ValueTok) rightVar).value.asLong();
////        if (rightVar instanceof IntegerExp) {
////            n2 = ((IntegerExp) rightVar).val.longValue();
////        } else {
////            n2 = ((LongExp) rightVar).val;
////        }
//
//        switch (op) {
//            case LT:
//                return n1.compareTo(n2) < 0;
//            case LE:
//                return n1.compareTo(n2) <= 0;
//            case GT:
//                return n1.compareTo(n2) > 0;
//            case GE:
//                return n1.compareTo(n2) >= 0;
//            case EQ:
//                return n1.compareTo(n2) == 0;
//            case NEQ:
//                return n1.compareTo(n2) != 0;
//            default:
//                return false; //err!
//        }
//    }
//}