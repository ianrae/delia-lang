package org.delia.tok;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.util.DeliaExceptionHelper;

import java.util.Collections;

public class TokClauseBuilder extends ServiceBase {

    public TokClauseBuilder(FactoryService factorySvc) {
        super(factorySvc);
    }

    public Tok.RuleTok buildRule(Exp.RuleClause ruleClause) {
        if (ruleClause.where instanceof Exp.DottedExp) {
            FieldChainVisitor visitor = new FieldChainVisitor();
            Exp.DottedExp dexp = (Exp.DottedExp) ruleClause.where;
            visitor.top = dexp;
            ruleClause.visit(visitor);

            Tok.DottedTok dotted = new Tok.DottedTok();
            for (Tok.FieldTok field : visitor.fieldStack) {
//                    Tok.FieldTok field = visitor.fieldStack.pop();
                if (field.fieldName.equals(FieldChainVisitor.SCALAR_FIELD)) {
                    dotted.chainL.addAll(field.funcL);
                } else {
                    dotted.chainL.add(field);
                }
            }
            Collections.reverse(dotted.chainL);
            Tok.RuleTok ruleTok = new Tok.RuleTok(dotted);
            return ruleTok;
        } else if (ruleClause.where instanceof Exp.OperatorExp) {
            Exp.OperatorExp oexp = (Exp.OperatorExp) ruleClause.where;
            OpVisitor visitor = new OpVisitor();
            visitor.top = oexp;
            oexp.visit(visitor);

            Tok.OperatorTok optok = visitor.optok1;
            Tok.RuleTok ruleTok = new Tok.RuleTok(optok);
            return ruleTok;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("x454");
            return null;
        }
    }

    public Tok.WhereTok buildWhere(Exp.WhereClause whereClause) {
        if (whereClause.where instanceof Exp.DottedExp) {
            FieldChainVisitor visitor = new FieldChainVisitor();
            Exp.DottedExp dexp = (Exp.DottedExp) whereClause.where;
            visitor.top = dexp;
            whereClause.visit(visitor);

            if (visitor.fieldStack.isEmpty()) {
                if (visitor.oneVal == null) {
                    return doCompositeKey(whereClause, visitor);
                }
                Tok.DToken tok = visitor.oneVal;
                if (tok instanceof Tok.ValueTok) {
                    Tok.ValueTok vexp = (Tok.ValueTok) tok;
                    if (vexp.value.asString().equals("true")) {
                        Tok.DottedTok dd = new Tok.DottedTok();
                        dd.chainL.add(vexp);
                        Tok.WhereTok whereTok = new Tok.WhereTok(dd);
                        return whereTok;
                    } else {
                        Tok.PKWhereTok pkWhereTok = new Tok.PKWhereTok();
                        pkWhereTok.value = vexp;
                        Tok.WhereTok whereTok = new Tok.WhereTok(pkWhereTok);
                        return whereTok;
                    }
                } else if (tok instanceof Tok.FieldTok) {
                    Tok.FieldTok field = (Tok.FieldTok) tok;
                    Tok.DottedTok dotted = new Tok.DottedTok();
                    dotted.chainL.add(field);
                    Collections.reverse(dotted.chainL);
                    Tok.WhereTok whereTok = new Tok.WhereTok(dotted);
                    return whereTok;
                } else {
                    DeliaExceptionHelper.throwNotImplementedError("23432");
                    return null;
                }
            } else {
                Tok.DottedTok dotted = new Tok.DottedTok();
                for (Tok.FieldTok field : visitor.fieldStack) {
                    if (field.fieldName.equals(FieldChainVisitor.SCALAR_FIELD)) {
                        dotted.chainL.addAll(field.funcL);
                    } else {
                        dotted.chainL.add(field);
                    }
                }
                Collections.reverse(dotted.chainL);
                Tok.WhereTok whereTok = new Tok.WhereTok(dotted);
                return whereTok;
            }
        } else if (whereClause.where instanceof Exp.OperatorExp) {
            Exp.OperatorExp oexp = (Exp.OperatorExp) whereClause.where;
            OpVisitor visitor = new OpVisitor();
            visitor.top = oexp;
            oexp.visit(visitor);

            Tok.OperatorTok optok = visitor.optok1;
            Tok.WhereTok whereTok = new Tok.WhereTok(optok);
            return whereTok;
        } else {
            DeliaExceptionHelper.throwNotImplementedError("x454");
            return null;
        }
    }

    private Tok.WhereTok doCompositeKey(Exp.WhereClause whereClause, FieldChainVisitor visitor) {
        Tok.PKWhereTok pkWhereTok = new Tok.PKWhereTok();
        pkWhereTok.listValue = visitor.listTok;
        Tok.WhereTok whereTok = new Tok.WhereTok(pkWhereTok);
        return whereTok;
    }

    public Tok.DottedTok buildFieldsAndFuncs(Exp.DottedExp dexp) {
        FieldChainVisitor visitor = new FieldChainVisitor();
        visitor.top = dexp;
        dexp.visit(visitor);

        Tok.DottedTok dotted = new Tok.DottedTok();
        for (Tok.FieldTok field : visitor.fieldStack) {
            if (field.fieldName.equals(FieldChainVisitor.SCALAR_FIELD)) {
                dotted.chainL.addAll(field.funcL);
            } else {
                dotted.chainL.add(field);
            }
        }
        //Collections.reverse(dotted.chainL);
        return dotted;
    }

}
