package org.delia.compiler.ast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.delia.hld.HLD;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.StrCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Holder for exp classes
 */
public class Exp {
    public static class JoinInfo {
        public DTypeName leftTypeName;
        public DTypeName rightTypeName;
        public DStructType leftType; //set later
        public DStructType rightType; //""
        public String throughField; //field of leftType
        public String alias; //set later
        public boolean isFKOnly; //only getting the fk
        public boolean isFetch;
        public boolean isTransitive; //true means we are joining through an assoc table to an entity table
        @JsonIgnore
        public RelationInfo relinfo; //set later

        public JoinInfo(DTypeName leftTypeName, DTypeName rightTypeName, String throughField) {
            this.leftTypeName = leftTypeName;
            this.rightTypeName = rightTypeName;
            this.throughField = throughField;
        }

        public boolean isSelfJoin() {
            return leftType == rightType;
        }
        public boolean isMatch(JoinInfo other) {
            if (leftType.equals(other.leftType)) {
                if (rightType.equals(other.rightType)) {
                    if (throughField.equals(other.throughField)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s.%s.%s", leftTypeName, throughField, rightTypeName);
        }
        //and join type
    }

    //we'll keep a map<int,JoinInfo> where int is joinId
    //then In HLD we can use map<int,HLDJoinInfo>
    // -so FieldExp.joinId stays the same
    //same in LLD. map<int,LLDJoinInfo>
    //or
    //leave FieldExp.joinInfo and then in HLD we create map<JoinInfo,HLDJoinInfo>
    public interface ExpVisitor {
        void visit(ExpBase exp);
    }

    public interface ExpBase {
        //        int getPos(); for error messages
        void visit(ExpVisitor visitor);
    }

    public interface ElementExp extends ExpBase {
        String strValue();
    }

    //can be a var (let x = 5) or a fieldName .addr
    public static class FieldExp implements ElementExp {
        public String fieldName;
        public JoinInfo joinInfo; //can be null (TODO: i don't think we ever set this!)
        public String alias;
        public DStructType assocPhysicalType; //for MM only. set later
//        public DStructType ownerType; //what type is this a field of. set later
//        public JoinInfo ownerFoundInJoinInfo;

        public FieldExp(String fieldName, JoinInfo joinInfo) {
//            this.typeName = typeName;
            this.fieldName = fieldName;
            this.joinInfo = joinInfo;
        }

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String strValue() {
            return alias == null ? fieldName : String.format("%s.%s", alias, fieldName);
        }

        @Override
        public String toString() {
            return String.format("%s", fieldName);
        }
    }

    public static class ValueExp implements ElementExp {
        public DValue value;
        public TypePair hintPair; //set later
//        public DStructType pkOwnerType; //only used for pk query (Customer[55])
//        public String pkOwnerAlias;

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
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

    public static class NullExp implements ElementExp {

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
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

    public static class FunctionExp implements ElementExp {
        public String fnName;
        public List<ElementExp> argsL = new ArrayList<>();
        public String prefix; //"wid.distinct" would put "wid" here
        public boolean negFlag; //only used on rules

        public FunctionExp(String fnName) {
            this.fnName = fnName;
        }

        public boolean isName(String s) {
            return s.equals(fnName);
        }

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
            for (ElementExp arg : argsL) {
                arg.visit(visitor);
            }
        }

        @Override
        public String strValue() {
            StrCreator sc = new StrCreator();
            sc.addStr(fnName);
            sc.addStr("(");
            int index = 0;
            for (ElementExp arg : argsL) {
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
            for (ElementExp arg : argsL) {
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

    public static class ListExp implements ElementExp {
        public List<ElementExp> listL = new ArrayList<>();

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
            for (ElementExp exp : listL) {
                exp.visit(visitor);
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

    public static class CompositeKeyExp implements ElementExp {
        public List<ElementExp> listL = new ArrayList<>();

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
            for (ElementExp exp : listL) {
                exp.visit(visitor);
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

    public interface OperandExp extends ExpBase {
        String strValue();
    }

    //x or x.x ...
    public static class DottedExp implements OperandExp {
        public List<ElementExp> chainL = new ArrayList<>();

        public DottedExp() {
        }

        public DottedExp(ElementExp single) {
            chainL.add(single);
        }

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
            for (ElementExp exp : chainL) {
                exp.visit(visitor);
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

    public static class OperatorExp implements OperandExp {
        public OperandExp op1;
        public OperandExp op2;
        public String op;
        public boolean negFlag;

        @Override
        public void visit(ExpVisitor visitor) {
            visitor.visit(this);
            op1.visit(visitor);
            op2.visit(visitor);
        }

        @Override
        public String strValue() {
            String s = String.format("%s %s %s", op1.strValue(), op, op2.strValue());
            return negFlag ? String.format("!(%s)", s) : s;
        }

        @Override
        public String toString() {
            return strValue();
        }
    }

    public static class WhereClause {
        public OperandExp where;

        public WhereClause(OperandExp exp) {
            this.where = exp;
        }

        public void visit(ExpVisitor visitor) {
            where.visit(visitor);
        }

        public String strValue() {
            return where.strValue();
        }

        @Override
        public String toString() {
            return where.toString();
        }
    }

    public static class RuleClause {
        public OperandExp where;

        public RuleClause(OperandExp exp) {
            this.where = exp;
        }

        public void visit(ExpVisitor visitor) {
            where.visit(visitor);
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
