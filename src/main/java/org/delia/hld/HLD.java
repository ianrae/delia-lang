package org.delia.hld;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Holder for HLD classes
 */
public class HLD {

    //represents element in select list. can be field or a function (such as count(*))
    public interface HLDEx {
    }

    public static class HLDFuncEx implements HLDEx {
        public String fnName;
        public List<HLD.HLDEx> argsL = new ArrayList<>(); //contains HLDFuncEx or HLDFieldValue or HLDField, HLDFuncArg

        public HLDFuncEx(String fnName) {
            this.fnName = fnName;
        }
    }
//    public static class HLDFinalFieldEx implements HLDEx {
//        public DStructType hldTable;
//        public TypePair pair; //eg. String firstName
//        @JsonIgnore
//        public final RelationInfo relinfo; //can be null
//
//        public HLDFinalFieldEx(DStructType hldTable, TypePair pair, RelationInfo relinfo) {
//            this.hldTable = hldTable;
//            this.pair = pair;
//            this.relinfo = relinfo;
//        }
//
//        public boolean isPK() {
//            return hldTable.fieldIsPrimaryKey(pair.name);
//        }
//    }
    public static class HLDFuncArg implements HLDEx {
        public String argVal; //something like 'asc' or 'desc'
        public DValue dval; //or this has a value

        public HLDFuncArg(String argVal) {
            this.argVal = argVal;
        }
    }

    //keep this simple. just type and name
    public static class HLDField implements HLDEx {
        public DStructType hldTable;
        public TypePair pair; //eg. String firstName
        public CrudAction crudAction; //update statements only
        @JsonIgnore
        public final RelationInfo relinfo; //can be null
        public HLDJoin finalJoin; //only set if this is .finalField

        public HLDField(DStructType hldTable, TypePair pair, RelationInfo relinfo) {
            this.hldTable = hldTable;
            this.pair = pair;
            this.relinfo = relinfo;
        }

        public boolean isPK() {
            return hldTable.fieldIsPrimaryKey(pair.name);
        }
    }

    //used in insert and update statements
    public static class HLDFieldValue implements HLDEx {
        public HLDField hldField;
        public DValue dvalue; //if set then dvalueList is null
        public List<DValue> dvalueList; //if set then dvalue is null

        public HLDFieldValue(HLDField field, DValue dval) {
            this.hldField = field;
            this.dvalue = dval;
        }
    }

    public static class HLDJoin {
        public Exp.JoinInfo joinInfo;
        public List<HLDField> fields = new ArrayList<>(); //fields in the joined table that we need to get
        //fields could be empty, or just have pk (fk), or all the fields
    }


    public interface HLDStatement {
        AST.Loc getLoc();
    }
    public static class HLDStatementBase implements HLDStatement {
        public AST.Loc loc;

        @Override
        public AST.Loc getLoc() { return loc; }
    }
    public static class SchemaHLDStatement extends HLDStatementBase {
        public String schema;

        public SchemaHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("SchemaHLD: '%s'", schema);
        }
    }

    //create a table
    public static class TypeHLDStatement extends HLDStatementBase {
        public DStructType hldTable;
        public List<HLDField> fields = new ArrayList<>();

        public TypeHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("TypeHLD: '%s'", hldTable.getName());
        }
    }

    public static class InsertHLDStatement extends HLDStatementBase {
        public DStructType hldTable;
        public List<HLDFieldValue> fields = new ArrayList<>();
        public String syntheticIdField;
        public DValue syntheticIDValue;

        public InsertHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("InsertHLD: '%s'", hldTable.getName());
        }
    }

    public static class LetAssignHLDStatement extends HLDStatementBase {
        public DType dtype; //  mainStructType; //C[].addr then fromType is A and mainStringType is C
        public String varName;
        public String rhsExpr; //if doing let x = z
        public DValue dvalue;

        public LetAssignHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }
    }

    public static class LetHLDStatement extends HLDStatementBase {
        public DStructType hldTable; //  mainStructType; //C[].addr then fromType is A and mainStringType is C
        public List<HLDEx> fields = new ArrayList<>();
//        public Exp.WhereClause whereClause;
        public Tok.WhereTok whereTok; //in parallel with whereClause for now
        public List<HLDJoin> joinL = new ArrayList<>(); //logical joins (and their fields)
        public HLDField finalField = null;
        public List<HLDEx> finalFieldsL = new ArrayList<>();
        public Tok.DottedTok fieldAndFuncs;
        public String varName;

        public DStructType fromType;
        public DType resultType; //might be string if .firstName

        public LetHLDStatement(DStructType hldTable, AST.Loc loc) {
            this.hldTable = hldTable;
            this.fromType = hldTable;
            this.resultType = hldTable;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("LetHLD: %s[%s]", hldTable.getName(), whereTok.toString());
        }
    }

    public static class DeleteHLDStatement extends HLDStatementBase {
        public DStructType hldTable; //  mainStructType; //C[].addr then fromType is A and mainStringType is C
        public Exp.WhereClause whereClause;
        public Tok.WhereTok whereTok;

        public DeleteHLDStatement(DStructType hldTable, AST.Loc loc) {
            this.hldTable = hldTable;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("DeleteHLD: %s[%s]", hldTable.getName(), whereClause.toString());
        }
    }



    //dto to allow common code
    public static abstract class HLDUpdateUpsertBase extends HLDStatementBase {
        public DStructType hldTable; //  mainStructType; //C[].addr then fromType is A and mainStringType is C
        public Exp.WhereClause whereClause;
        public Tok.WhereTok whereTok; //in parallel with whereClause for now
        public List<HLDFieldValue> fields = new ArrayList<>();
    }

    public static class UpdateHLDStatement extends HLDUpdateUpsertBase {

        public UpdateHLDStatement(DStructType hldTable, AST.Loc loc) {
            this.hldTable = hldTable;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("UpdateHLD: %s[%s]", hldTable.getName(), whereClause.toString());
        }
    }
    public static class UpsertHLDStatement extends HLDUpdateUpsertBase {
        public HLDFieldValue fieldPK;
        public boolean noUpdateFlag;

        public UpsertHLDStatement(DStructType hldTable, AST.Loc loc) {
            this.hldTable = hldTable;
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("UpsertHLD: %s[%s]", hldTable.getName(), whereClause.toString());
        }
    }

    public static class ConfigureHLDStatement extends HLDStatementBase {
        public String configName;
        public DValue dval;

        public ConfigureHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }

        @Override
        public String toString() {
            return String.format("CongigHLD: %s=%s", configName, dval.asString());
        }
    }

    public static class LogHLDStatement extends HLDStatementBase {
        public String varName;
        public DValue dval;

        public LogHLDStatement(AST.Loc loc) {
            this.loc = loc;
        }

        @Override
        public String toString() {
            String s = dval == null ? "" : dval.asString();
            return String.format("LogHLD: %s %s", varName, s);
        }
    }
}