package org.delia.tok;


import org.delia.compiler.ast.Exp;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.StrCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Tok {

    public interface TokVisitor {
        void visit(Tok.TokBase exp, Tok.TokBase parent);
    }

    public interface TokBase {
        //        int getPos(); for error messages
        void visit(Tok.TokVisitor visitor, Tok.TokBase parent);
    }

    public interface DToken extends Tok.TokBase {
        String strValue();
    }

    public static class FunctionTok implements Tok.DToken {
        public String fnName;
        public List<Tok.DToken> argsL = new ArrayList<>();
        public boolean negFlag; //for rules only

        public FunctionTok(String fnName) {
            this.fnName = fnName;
        }

        public boolean isName(String s) {
            return s.equals(fnName);
        }

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            for (Tok.DToken arg : argsL) {
                arg.visit(visitor, this);
            }
        }

        @Override
        public String strValue() {
            StrCreator sc = new StrCreator();
            sc.addStr(fnName);
            sc.addStr("(");
            int index = 0;
            for (Tok.DToken arg : argsL) {
                if (index > 0) {
                    sc.addStr(", ");
                }
                sc.o(arg.strValue());
                index++;
            }
            sc.addStr(")");
            return sc.toString();
        }

        @Override
        public String toString() {
            StrCreator sc = new StrCreator();
            sc.addStr(fnName);
            sc.addStr("(");
            int index = 0;
            for (Tok.DToken arg : argsL) {
                if (index > 0) {
                    sc.addStr(", ");
                }
                sc.o(arg.toString());
                index++;
            }
            sc.addStr(")");
            return sc.toString();
        }
    }

    //can be a var (let x = 5) or a fieldName .addr
    public static class FieldTok implements Tok.DToken {
        public String fieldName;
        public Exp.JoinInfo joinInfo; //can be null
        public String alias;
        public DStructType assocPhysicalType; //for MM only. set later
        public DStructType ownerType; //what type is this a field of. set later
        public List<FunctionTok> funcL = new ArrayList<>();
        public Exp.JoinInfo ownerFoundInJoinInfo;

        public FieldTok(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
        }

        @Override
        public String strValue() {
            return alias == null ? fieldName : String.format("%s.%s", alias, fieldName);
        }

        @Override
        public String toString() {
            if (! funcL.isEmpty()) {
                StringJoiner joiner = new StringJoiner(".");
                funcL.forEach(exp -> joiner.add(exp.toString()));
                return String.format("%s.%s", fieldName, joiner.toString());
            } else {
                return String.format("%s", fieldName);
            }
        }
    }

    public static class ValueTok implements Tok.DToken {
        public DValue value;
        public TypePair hintPair; //set later

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
        }

        @Override
        public String strValue() {
            return value == null ? "null" : value.asString();
        }

        @Override
        public String toString() {
            return value == null ? "null" : String.format("%s", value.asString());
        }
    }

    public static class NullTok implements Tok.DToken {

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
        }

        @Override
        public String strValue() {
            return "null";
        }

        @Override
        public String toString() {
            return "null";
        }
    }


    public static class ListTok implements Tok.DToken {
        public List<Tok.DToken> listL = new ArrayList<>();

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            for (Tok.DToken exp : listL) {
                exp.visit(visitor, this);
            }
        }

        @Override
        public String strValue() {
            StringJoiner joiner = new StringJoiner(",");
            listL.forEach(exp -> joiner.add(exp.strValue()));
            return joiner.toString();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(",");
            listL.forEach(exp -> joiner.add(exp.toString()));
            return joiner.toString();
        }
    }

    public static class CompositeKeyTok implements Tok.DToken {
        public List<Tok.DToken> listL = new ArrayList<>();

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            for (Tok.DToken exp : listL) {
                exp.visit(visitor, this);
            }
        }

        @Override
        public String strValue() {
            StringJoiner joiner = new StringJoiner(",");
            listL.forEach(exp -> joiner.add(exp.strValue()));
            return joiner.toString();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(",");
            listL.forEach(exp -> joiner.add(exp.toString()));
            return joiner.toString();
        }
    }

    public interface OperandTok extends Tok.TokBase {
        String strValue();
    }

    //x or x.x ...
    public static class DottedTok implements Tok.OperandTok {
        public List<Tok.DToken> chainL = new ArrayList<>();

        public DottedTok() {
        }

        public DottedTok(Tok.DToken single) {
            chainL.add(single);
        }

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            for (Tok.DToken exp : chainL) {
                exp.visit(visitor, this);
            }
        }

        @Override
        public String strValue() {
            StringJoiner joiner = new StringJoiner(",");
            chainL.forEach(exp -> joiner.add(exp.strValue()));
            return joiner.toString();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(",");
            chainL.forEach(exp -> joiner.add(exp.toString()));
            return joiner.toString();
        }
    }

    public static class PKWhereTok implements Tok.OperandTok {
        public Tok.ValueTok value;
        public Tok.CompositeKeyTok compositeKeyTok; //for composite keys
        public DStructType pkOwnerType;
        public String alias;
        public String physicalFieldName; //usually Customer.id but in MM can be CustomerAddressDat1.leftv
        public PrimaryKey primaryKey; //used for composite keys

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            if (value != null) {
                value.visit(visitor, this);
            }
            if (compositeKeyTok != null) {
                compositeKeyTok.visit(visitor, this);
            }
        }

        public boolean isCompositeKey() {
            return compositeKeyTok != null;
        }

        @Override
        public String strValue() {
            if (isCompositeKey()) {
                String s = compositeKeyTok.strValue();
                return s;
            }
            String s = String.format("%s", value.strValue());
            return s;
        }

        @Override
        public String toString() {
            return strValue();
        }
    }
    public static class OperatorTok implements Tok.OperandTok {
        public Tok.OperandTok op1;
        public Tok.OperandTok op2;
        public String op;
        public boolean negFlag;

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            visitor.visit(this, parent);
            op1.visit(visitor, this);
            op2.visit(visitor, this);
        }

        @Override
        public String strValue() {
            String s = String.format("%s %s %s", op1.strValue(), op, op2.strValue());
            return negFlag ? String.format("!(%s)", s) : s;
        }

        @Override
        public String toString() {
            String op1Str = op1 == null ? "null" : op1.toString();
            String op2Str = op2 == null ? "null" : op2.toString();
            String s = String.format("%s %s %s", op1Str, op, op2Str);
            return negFlag ? String.format("!(%s)", s) : s;
        }
    }

    public static class WhereTok {
        public Tok.OperandTok where;

        public WhereTok(Tok.OperandTok exp) {
            this.where = exp;
        }

        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            where.visit(visitor, parent);
        }

        public String strValue() {
            return where.strValue();
        }

        @Override
        public String toString() {
            return where.toString();
        }
    }

    public static class RuleTok {
        public Tok.OperandTok where;

        public RuleTok(Tok.OperandTok exp) {
            this.where = exp;
        }

        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
            where.visit(visitor, parent);
        }

        public String strValue() {
            return where.strValue();
        }

        @Override
        public String toString() {
            return where.toString();
        }
    }
    
}
