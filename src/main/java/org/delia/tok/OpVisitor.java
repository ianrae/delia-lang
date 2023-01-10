package org.delia.tok;

import org.delia.compiler.ast.Exp;
import org.delia.util.DeliaExceptionHelper;

import java.util.Collections;
import java.util.Stack;

public class OpVisitor implements Exp.ExpVisitor {

    public Exp.ExpBase top;
    public Exp.ExpBase prev;
    public Tok.OperatorTok optok1;
    public Stack<Tok.OperatorTok> opStack = new Stack<>();

    @Override
    public void visit(Exp.ExpBase exp) {
        if (exp instanceof Exp.OperatorExp) {
            Exp.OperatorExp oexp = (Exp.OperatorExp) exp;

            if (oexp.op1 instanceof Exp.OperatorExp) {
                if (opStack.isEmpty()) {
                    Tok.OperatorTok optok = new Tok.OperatorTok();
                    opStack.push(optok);
                    optok.negFlag = oexp.negFlag;
                    optok.op = oexp.op;

                    if (oexp.op2 instanceof Exp.OperatorExp) {
                        //do nothing. we'll resolve lower down
                    } else {
                        optok.op2 = doOp(oexp.op2);
                    }
                } else {
//                    DeliaExceptionHelper.throwNotImplementedError("5544y");
                    Tok.OperatorTok optok = new Tok.OperatorTok();
                    opStack.push(optok);
                    optok.negFlag = oexp.negFlag;
                    optok.op = oexp.op;

                    if (oexp.op2 instanceof Exp.OperatorExp) {
                        //do nothing. we'll resolve lower down
                    } else {
                        optok.op2 = doOp(oexp.op2);
                    }
                }
            } else if (oexp.op2 instanceof Exp.OperatorExp) {
                if (opStack.isEmpty()) {
                    Tok.OperatorTok optok = new Tok.OperatorTok();
                    opStack.push(optok);
                    optok.negFlag = oexp.negFlag;
                    optok.op = oexp.op;

                    if (oexp.op1 instanceof Exp.OperatorExp) {
                        //do nothing. we'll resolve lower down
                    } else {
                        optok.op1 = doOp(oexp.op1);
                    }
                } else {
//                    DeliaExceptionHelper.throwNotImplementedError("5544y2");
                    Tok.OperatorTok optok = new Tok.OperatorTok();
                    opStack.push(optok);
                    optok.negFlag = oexp.negFlag;
                    optok.op = oexp.op;

                    if (oexp.op1 instanceof Exp.OperatorExp) {
                        //do nothing. we'll resolve lower down
                    } else {
                        optok.op1 = doOp(oexp.op1);
                    }
                }
            } else {
                Tok.OperatorTok optok = new Tok.OperatorTok();
                optok1 = optok;
                optok.negFlag = oexp.negFlag;
                optok.op = oexp.op;

                optok.op1 = doOp(oexp.op1);
                optok.op2 = doOp(oexp.op2);

                if (!opStack.isEmpty()) {
                    Tok.OperatorTok poppedTok = opStack.peek();
                    if (poppedTok.op1 == null) {
                        poppedTok.op1 = optok;
                    } else if (poppedTok.op2 == null) {
                        poppedTok.op2 = optok;
                    }

                    if (opStack.size() > 1) {
                        int n = opStack.size();
                        Tok.OperatorTok xTok = opStack.get(n - 2); //one below TOS
                        if (poppedTok.op1 != null && poppedTok.op2 != null) {
                            if (xTok.op1 == null) {
                                xTok.op1 = poppedTok;
                            } else if (xTok.op2 == null) {
                                xTok.op2 = optok;
                            }

                            if (xTok.op1 != null & xTok.op2 != null) {
                                opStack.pop();
                                this.optok1 = xTok;
                            } else {
                                this.optok1 = poppedTok;
                            }
                        }
                    } else if (poppedTok.op1 != null && poppedTok.op2 != null) {
//                        opStack.pop();
                        this.optok1 = poppedTok;
                    }
                }
            }
        }
        prev = exp;
    }

    private Tok.OperandTok doOp(Exp.OperandExp op1) {
        if (op1 instanceof Exp.DottedExp) {
            FieldChainVisitor visitor = new FieldChainVisitor();
            Exp.DottedExp dexp = (Exp.DottedExp) op1;
            visitor.top = dexp;
            op1.visit(visitor);

            Tok.DottedTok dotted = new Tok.DottedTok();
            if (visitor.fieldStack.isEmpty()) {
                if (visitor.listTok != null) {
                    dotted.chainL.add(visitor.listTok);
                } else {
                    dotted.chainL.add(visitor.oneVal);
                }
            } else {
                for (Tok.FieldTok field : visitor.fieldStack) {
//                        Tok.FieldTok field = visitor.fieldStack.pop();
                    if (field.fieldName.equals(FieldChainVisitor.SCALAR_FIELD)) {
                        dotted.chainL.addAll(field.funcL);
                    } else {
                        dotted.chainL.add(field);
                    }
                }
                Collections.reverse(dotted.chainL);
            }
            return dotted;
        } else if (op1 instanceof Exp.OperatorExp) {
            Exp.OperatorExp oexp = (Exp.OperatorExp) op1;
            OpVisitor visitor = new OpVisitor();
            visitor.top = oexp;
            oexp.visit(visitor);
            return visitor.optok1;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("other op type");
            return null;
        }
    }
}
