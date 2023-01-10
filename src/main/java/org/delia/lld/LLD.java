package org.delia.lld;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.db.SqlStatement;
import org.delia.hld.CrudAction;
import org.delia.hld.HLD;
import org.delia.hld.dat.AssocSpec;
import org.delia.relation.RelationInfo;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.dbimpl.mem.impl.QueryType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * LLD - Low-level description
 * -represents the SQL elements that delia uses
 * -LLD knows nothing about HLD
 * -the idea is that LL can render every type of SQL that we need
 */
public class LLD {

    public static interface LLElement {
        String getSQLName();
    }

    public interface LLNameFormatter {
        String formatName(String name); //some databases use UPPERCASE
        String formatName(String schema, String name); //some databases use UPPERCASE
    }

    public static class DefaultLLNameFormatter implements LLNameFormatter {
        @Override
        public String formatName(String name) {
            return name == null ? null : name.toLowerCase(Locale.ROOT); //postgres likes lower-case
        }

        @Override
        public String formatName(String schema, String name) {
            if (schema == null) {
                return formatName(name);
            }
            String s = String.format("%s.%s", schema, name);
            return name == null ? null : s.toLowerCase(Locale.ROOT); //postgres likes lower-case
        }
    }

    //represents element in select list. can be field or a function (such as count(*))
    public interface LLEx extends LLElement {
    }

    public static class LLDFuncEx implements LLEx {
        public String fnName;
        public List<LLD.LLEx> argsL = new ArrayList<>(); //LLFuncEx or LLFinalFieldEx or LLFuncArg

        public LLDFuncEx(String fnName) {
            this.fnName = fnName;
        }

        @Override
        public String getSQLName() {
            return null;
        }
    }
    public static class LLFinalFieldEx implements LLEx {
        public LLTable physicalTable; //not a clone, so get alias automatically
        public String fieldName;
        public HLD.HLDJoin finalJoin;

        public LLFinalFieldEx(LLTable llTable, String fieldName) {
            this.physicalTable = llTable;
            this.fieldName = fieldName;
        }

        @Override
        public String getSQLName() {
            return null;
        }
    }
    public static class LLFuncArg implements LLEx {
        public String funcArg;
        public DValue dval; //or this has a value

        public LLFuncArg(String funcArg) {
            this.funcArg = funcArg;
        }

        @Override
        public String getSQLName() {
            return null;
        }
    }

    public static class LLTable implements LLElement {
        public DStructType logicalType;
        public DStructType physicalType; //holds schema
        public String alias; //can be null
        @JsonIgnore
        public LLNameFormatter formatter;

        public LLTable(DStructType logicalType, DStructType physicalType, LLNameFormatter formatter) {
            this.logicalType = logicalType;
            this.physicalType = physicalType;
            this.formatter = formatter;
        }
        public LLTable clone() {
            LLTable clone = new LLTable(logicalType, physicalType, formatter);
            clone.alias = alias;
            return clone;
        }

        @Override
        public String getSQLName() {
            return formatter.formatName(physicalType.getSchema(), physicalType.getName());
        }

        @Override
        public String toString() {
            String s = String.format("%s: phys:%s, alias:%s", logicalType.getName(), physicalType.getName(), alias);
            return s;
        }
    }

    public static class LLField implements LLEx {
        public LLTable physicalTable; //physical is this ever null?
        public TypePair physicalPair; //physical. eg String firstname
        public String asName; //can be null
        public boolean isAssocField;
        public int columnIndex; //added when parse result set. Is this thread-safe? is this only used by one statement?
        public Exp.JoinInfo joinInfo; //set later if a join field
        public CrudAction crudAction; //update statements only

        public String getFieldName() {
            return physicalPair.name;
        }

        @JsonIgnore
        public LLNameFormatter formatter;

        public LLField(TypePair pair, LLTable physicalTable, LLNameFormatter formatter) {
            this.physicalPair = pair;
            //make a copy of physical table because of self-joins (where same 'table' has different aliases)
            this.physicalTable = physicalTable.clone();
            this.formatter = formatter;
        }

        public String getTableName() {
            return physicalTable.physicalType.getName();
        }

        @Override
        public String getSQLName() {
            return formatter.formatName(getTableName());
        }

        @Override
        public String toString() {
            String s = String.format("phys:%s.%s", physicalTable.physicalType.getName(), physicalPair.name);
            return s;
        }

    }

    public static class LLFieldValue implements LLEx {
        public LLField field;
        public DValue dval;
        public List<DValue> dvalList;

        public LLFieldValue(LLField field, DValue dvalue) {
            this.field = field;
            this.dval = dvalue;
        }

        @Override
        public String getSQLName() {
            return null; //TODO hmmm
        }
    }

    //physical join: C.adrr.A.id or C.addr.CustomerAddressDat.rightv
    public static class LLJoin {
        public Exp.JoinInfo logicalJoin;
        public LLField physicalLeft; //physical Table.field. holds the throughField
        public LLField physicalRight; //""

        public List<LLField> physicalFields = new ArrayList<>(); //physical fields in the right table that we need to get

        public boolean isSelfJoin() {
            return logicalJoin.isSelfJoin();
        }
    }

    public interface LLStatementRenderer {
        SqlStatement render(LLCreateSchema statement);
        SqlStatement render(LLSelect statement);
        SqlStatement render(LLDelete statement);
        SqlStatement render(LLUpdate statement);
        SqlStatement render(LLUpsert statement);

        SqlStatement render(LLInsert statement);

        SqlStatement render(LLCreateTable statement);
        SqlStatement render(LLCreateAssocTable statement);
    }

    public interface LLStatement {
        SqlStatement render(LLStatementRenderer renderer);
        void setSql(SqlStatement sql);
        SqlStatement getSql();
        boolean requiresSql(); //some statements like LLAssign don't
        AST.Loc getLoc();
    }
    public interface HasLLTable {
        LLTable getTable();
    }

    public static abstract class LLStatementBase implements LLStatement {
        protected SqlStatement sql;
        protected AST.Loc loc;

        public LLStatementBase(AST.Loc loc) {
            this.loc = loc;
        }

        public abstract SqlStatement render(LLStatementRenderer renderer);

        @Override
        public void setSql(SqlStatement sql) {
            this.sql = sql;
        }

        @Override
        public SqlStatement getSql() {
            return sql;
        }

        @Override
        public boolean requiresSql() {
            return true; //any statement's that don't need SQL should override this
        }

        @Override
        public AST.Loc getLoc() {
            return loc;
        }
    }

    public static class LLCreateSchema extends LLStatementBase {
        public String schema; //creates if not exist

        public LLCreateSchema(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            return String.format("LLSchema %s", schema);
        }
    }
    public static class LLAssign extends LLStatementBase {
        public DType dtype;
        public String varName;
        public String rhsExpr; //if doing let x = z
        public DValue dvalue;

        public LLAssign(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return null; //no sql
        }
        @Override
        public String toString() {
            String valueStr = dvalue == null ? "null" : dvalue.asString();
            String rhsStr = rhsExpr == null ? "" : "rhs: " + rhsExpr;
            return String.format("LLAssign %s=%s %s", varName, valueStr, rhsStr);
        }

        @Override
        public boolean requiresSql() {
            return false;
        }
    }
    public static class LLSelect extends LLStatementBase implements HasLLTable {
        public String varName;
        public LLTable table;
        public List<LLEx> fields = new ArrayList<>();
        public List<LLJoin> joinL = new ArrayList<>();
        public List<LLD.LLEx> finalFieldsL = new ArrayList<>();
        public Tok.WhereTok whereTok;
        public DStructType whereAllOrPKType; //a hint
        //we'll set any FieldExp.alias here too
        //public Exp.DottedExp fieldAndFuncs;
        public DType resultType;

        public LLSelect(AST.Loc loc) {
            super(loc);
        }

        public String getTableName() {
            return table.getSQLName();
        }
        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLSelect %s [%s]", table.toString(), whereTok.toString());
            return s;
        }

        @Override
        public LLTable getTable() {
            return table;
        }
    }
    public static class LLDelete extends LLStatementBase implements HasLLTable{
        public LLTable table;
        public Tok.WhereTok whereTok;

        public LLDelete(AST.Loc loc) {
            super(loc);
        }

        public String getTableName() {
            return table.getSQLName();
        }
        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLDelete %s [%s]", table.toString(), whereTok.toString());
            return s;
        }

        @Override
        public LLTable getTable() {
            return table;
        }
    }
    public static abstract class LLUpdateUpsertBase extends LLStatementBase implements HasLLTable {
        public LLTable table;
        public Tok.WhereTok whereTok;
        public List<LLFieldValue> fieldL = new ArrayList<>();

        public LLUpdateUpsertBase(AST.Loc loc) {
            super(loc);
        }

        public String getTableName() {
            return table.getSQLName();
        }

        @Override
        public LLTable getTable() {
            return table;
        }
    }

    public static class LLUpdate extends LLUpdateUpsertBase {
        public LLUpdate(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }
        @Override
        public String toString() {
            String s = String.format("LLUpdate %s [%s]", table.toString(), whereTok.toString());
            return s;
        }
    }
    public static class LLUpsert extends LLUpdateUpsertBase {
        public LLFieldValue pkField;
        public boolean noUpdateFlag;

        public LLUpsert(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLUpsert %s [%s]", table.toString(), whereTok.toString());
            return s;
        }
    }


    public static class LLSubQueryInsertInfo {
        public List<LLFieldValue> subQueryFieldL;
        public DStructType subQueryStructType;
        public RelationInfo relinfo;
        public Tok.WhereTok whereTok;
        public QueryType queryType;

        public LLSubQueryInsertInfo(List<LLFieldValue> subQueryFieldL, DStructType subQueryStructType) {
            this.subQueryFieldL = subQueryFieldL;
            this.subQueryStructType = subQueryStructType;
        }
    }
    public static class LLInsert extends LLStatementBase implements HasLLTable {
        public LLTable table;
        public List<LLFieldValue> fieldL = new ArrayList<>();
        public LLFieldValue syntheticField;

        //special cases
        public LLSubQueryInsertInfo subQueryInfo;
        public RelationInfo assocUpdateRelinfo; //only used for assoc update

        public LLInsert(AST.Loc loc) {
            super(loc);
        }

        public String getTableName() {
            return table.getSQLName();
        }
        public boolean isSerialPK() {
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(table.physicalType);
            if (pkpair == null) return false;
            return table.physicalType.fieldIsSerial(pkpair.name);
        }
        public TypePair getSerialPKPair() {
            return DValueHelper.findPrimaryKeyFieldPair(table.physicalType);
        }

        public boolean areFieldsToInsert() {
            if (!fieldL.isEmpty()) {
                return true;
            }
            if (syntheticField != null) {
                return true;
            }
            DStructType structType = table.physicalType;
            List<TypePair> serialFields = structType.getAllFields().stream().filter(x -> structType.fieldIsSerial(x.name)).collect(Collectors.toList());
            return !serialFields.isEmpty();
        }


        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLInsert %s. %d fields", table.toString(), fieldL.size());
            return s;
        }

        @Override
        public LLTable getTable() {
            return table;
        }
    }

    //and do update,upsert,delete,merge

    public static class LLCreateTable extends LLStatementBase {
        public LLTable table;
        public List<LLField> fields = new ArrayList<>();

        public LLCreateTable(AST.Loc loc) {
            super(loc);
        }

        public String getTableName() {
            return table.getSQLName();
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLCreateTable %s. fields %d", table.toString(), fields.size());
            return s;
        }
    }
    public static class LLCreateAssocTable extends LLStatementBase {
        public AssocSpec assocSpec;

        public LLCreateAssocTable(AST.Loc loc) {
            super(loc);
        }
//        public List<LLField> fields = new ArrayList<>();

        public String getTableName() {
            return assocSpec.assocTblName;
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return renderer.render(this);
        }

        @Override
        public String toString() {
            String s = String.format("LLCreateAssocTable %s. fields %d", assocSpec.assocTblName, 2);
            return s;
        }
    }


    //drop,rename
    //field create,drop,rename,alter FIELD_RENAME_MANY_TO_MANY, FIELD_ALTER, FIELD_ALTER_TYPE
    /*
    		case INDEX_ADD:
		case INDEX_DELETE:
		case INDEX_ALTER:
		case CONSTRAINT_ADD:
		case CONSTRAINT_DELETE:
		case CONSTRAINT_ALTER:

     */

    public static class LLConfigure extends LLStatementBase {
        public String configName;
        public DValue dvalue;

        public LLConfigure(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return null; //no sql
        }
        @Override
        public String toString() {
            String valueStr = dvalue == null ? "null" : dvalue.asString();
            return String.format("LLConfigure %s=%s", configName, valueStr);
        }

        @Override
        public boolean requiresSql() {
            return false;
        }
    }

    public static class LLLog extends LLStatementBase {
        public String varName;
        public DValue dvalue;

        public LLLog(AST.Loc loc) {
            super(loc);
        }

        @Override
        public SqlStatement render(LLStatementRenderer renderer) {
            return null; //no sql
        }
        @Override
        public String toString() {
            String valueStr = dvalue == null ? "null" : dvalue.asString();
            return String.format("LLLog %s %s", varName, valueStr);
        }

        @Override
        public boolean requiresSql() {
            return false;
        }
    }
}
