package org.delia.dbimpl.mem.impl;

import org.delia.lld.processor.TokFieldHintVisitor;
import org.delia.tok.Tok;

public class QueryTypeDetector {

    public QueryType detectQueryType(Tok.WhereTok whereClause, QueryTypeDetails details) {
        if (wantsAllRows(whereClause)) {
            return QueryType.ALL_ROWS;
        } else if (isExpression(whereClause, details)) {
            return QueryType.OP;
        } else {
            return QueryType.PRIMARY_KEY;
        }
    }

    private boolean isExpression(Tok.WhereTok whereClause, QueryTypeDetails details) {
        if (whereClause.where instanceof Tok.OperatorTok) {
            if (details != null) { //want details?
                TokFieldHintVisitor visitor = new TokFieldHintVisitor(null, false);
                whereClause.visit(visitor, null);

                //TODO we currently only support one type in query. eg wid > 30. support more: wid > 30 && addr.city == 'toronto'
                for (Tok.FieldTok field : visitor.allFields) {
                    details.targetType = field.ownerType;
//                    details.ownerFoundInJoinInfo = field.ownerFoundInJoinInfo;
                }
            }

            return true;
        }
        return false;
    }

    private boolean wantsAllRows(Tok.WhereTok whereClause) {
        if (whereClause.where instanceof Tok.DottedTok) {
            Tok.DottedTok dexp = (Tok.DottedTok) whereClause.where;
            if (dexp.chainL.size() == 1) {
                Tok.DToken exp = dexp.chainL.get(0);
                if (exp instanceof Tok.ValueTok) {
                    Tok.ValueTok vexp = (Tok.ValueTok) exp;
                    if (vexp.strValue().equals("true")) {
                        return true;
                    }
                }
            }
        }
        return false;
        //Note we use to support Customer[] being equivalent to Customer[true] but don't anymore
    }

    /**
     * Detect if whereTok is an 'in' query with only DValue values.
     * @param whereTok
     * @param details
     * @return true if is an 'in' query with only DValue values.
     */
    public boolean isInExpression(Tok.WhereTok whereTok, InQueryTypeDetails details) {
        if (whereTok.where instanceof Tok.OperatorTok) {
            Tok.OperatorTok optok = (Tok.OperatorTok) whereTok.where;
            String target = "in";
            if (target.equals(optok.op)) {
                if (details != null) { //want details?
                    details.field = optok.op1.strValue();
                    details.allOp2AreValues = false;
                    if (optok.op2 instanceof Tok.DottedTok) {
                        Tok.DottedTok dotted = (Tok.DottedTok) optok.op2;
                        boolean fail = false;
                        for (Tok.DToken tok : dotted.chainL) {
                            if (tok instanceof Tok.ValueTok) {
                                Tok.ValueTok vv = (Tok.ValueTok) tok;
                                details.inValues.add(vv.value);
                            } else if (tok instanceof Tok.ListTok) {
                                Tok.ListTok ltok = (Tok.ListTok) tok;
                                for(Tok.DToken inner: ltok.listL) {
                                    if (inner instanceof Tok.ValueTok) {
                                        Tok.ValueTok vv = (Tok.ValueTok) inner;
                                        details.inValues.add(vv.value);
                                    } else {
                                        fail = true;
                                    }
                                }
                            } else {
                                fail = true;
                            }
                        }
                        details.allOp2AreValues = !fail;
                    }
                }
            }

            return true;
        }
        return false;
    }

}
