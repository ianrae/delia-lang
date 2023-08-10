package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LetSqlGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;
    private final DatService datSvc;
    private final DeliaOptions deliaOptions;
    private final SqlTableNameMapper sqlTableNameMapper;

    public LetSqlGenerator(FactoryService factorySvc, SqlValueRenderer sqlValueRenderer, ScalarValueBuilder valueBuilder, DatService datSvc, DeliaOptions deliaOptions, SqlTableNameMapper sqlTableNameMapper) {
        super(factorySvc);
        this.sqlValueRenderer = sqlValueRenderer;
        this.valueBuilder = valueBuilder;
        this.datSvc = datSvc;
        this.deliaOptions = deliaOptions;
        this.sqlTableNameMapper = sqlTableNameMapper;
    }

    public SqlStatement render(LLD.LLSelect statement) {
        StrCreator sc = new StrCreator();
        sc.o("SELECT ");

        if (hasFunc(statement, "exists")) {
            return renderExists(statement);
        } else if (hasFunc(statement, "min")) {
            return renderMinOrMax(statement, "min");
        } else if (hasFunc(statement, "max")) {
            return renderMinOrMax(statement, "max");
        }

        LLD.LLDFuncEx distinctExp = LLFieldHelper.findFunc(statement.finalFieldsL, "distinct");
        if (distinctExp != null) {
            sc.o("DISTINCT ");
        }

        ListWalker<LLD.LLEx> walker = new ListWalker<>(statement.fields);
        while (walker.hasNext()) {
            LLD.LLEx llex = walker.next();
            if (llex instanceof LLD.LLField) {
                LLD.LLField ff = (LLD.LLField) llex;
//                if (matchesField(distinctExp, ff)) {
//                    sc.o("DISTINCT ");
//                }

                String alias = ff.joinInfo == null ? ff.physicalTable.alias : ff.joinInfo.alias;
                sc.o("%s.%s", alias, ff.physicalPair.name);
            } else {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) llex;
                if (func.fnName.equals("count")) {
                    sc.addStr("count(*)");
                } else {
                    sc.o("%s()", func.fnName);
                }
            }
            walker.addIfNotLast(sc, ", ");
        }

        sc.o(" FROM %s", statement.table.getSQLName());
        sc.o(" as %s", statement.table.alias);

        renderJoins(sc, statement);

        SqlStatement sqlStatement = new SqlStatement();
        renderWhere(sc, statement, sqlStatement);

        LLD.LLDFuncEx orderBy = getOrderByIfPresent(statement);
        if (orderBy != null) {
            renderOrderBy(sc, statement, orderBy);
        } else if (deliaOptions != null && deliaOptions.autoSortByPK) { //only auto-sort if no other sorting defined
            boolean canBeOrdered = calcIfAreOrderableFields(statement);
            if (canBeOrdered) {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
                if (pkpair != null) {
                    String alias = statement.table.alias; //TODO: is this always non-null?
                    sc.o(" ORDER BY %s.%s", alias, pkpair.name);
                }
            }
        }

        if (hasFunc(statement, "first") || hasFunc(statement, "last")) {
            sc.o(" LIMIT 1");
        } else if (hasFunc(statement, "ith")) {
            doIth(sc, statement);
        }
        //TODO add rules for how all this mix together,
        //limit must be before offset
        if (hasFunc(statement, "limit")) {
            doLimit(sc, statement);
        }
        if (hasFunc(statement, "offset")) {
            doOffset(sc, statement);
        }

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    private void doIth(StrCreator sc, LLD.LLSelect statement) {
        LLD.LLDFuncEx func = LLFieldHelper.findFunc(statement.finalFieldsL, "ith");
        LLD.LLFuncArg fnArg = LLFieldHelper.getIthArg(func, 0);
        sc.o(" LIMIT 1 OFFSET %s", fnArg.dval.asString());
    }

    private void doOffset(StrCreator sc, LLD.LLSelect statement) {
        LLD.LLDFuncEx func = LLFieldHelper.findFunc(statement.finalFieldsL, "offset");
        LLD.LLFuncArg fnArg = LLFieldHelper.getIthArg(func, 0);
        sc.o(" OFFSET %s", fnArg.dval.asString());
    }

    private void doLimit(StrCreator sc, LLD.LLSelect statement) {
        LLD.LLDFuncEx func = LLFieldHelper.findFunc(statement.finalFieldsL, "limit");
        LLD.LLFuncArg fnArg = LLFieldHelper.getIthArg(func, 0);
        sc.o(" LIMIT %s", fnArg.dval.asString());
    }

    private boolean hasFunc(LLD.LLSelect statement, String fnName) {
        return LLFieldHelper.existsFunc(statement.finalFieldsL, fnName);
    }

    private SqlStatement renderExists(LLD.LLSelect statement) {
        //select exists(select true from contact where id=12)
        StrCreator sc = new StrCreator();
        sc.o("SELECT EXISTS(SELECT true ");
        sc.o(" FROM %s", statement.table.getSQLName());
        sc.o(" as %s", statement.table.alias);

        renderJoins(sc, statement);
        SqlStatement sqlStatement = new SqlStatement();
        renderWhere(sc, statement, sqlStatement);

        sc.o(")");
        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    private SqlStatement renderMinOrMax(LLD.LLSelect statement, String fnName) {
        //select min(wid) from contact where id=12
        LLD.LLDFuncEx func = LLFieldHelper.findFunc(statement.finalFieldsL, fnName);
        LLD.LLFuncArg fnArg = (LLD.LLFuncArg) func.argsL.get(0);
        StrCreator sc = new StrCreator();
        String sqlFnName = fnName.equals("min") ? "MIN" : "MAX";
        sc.o("SELECT %s(%s)", sqlFnName, fnArg.funcArg);
        sc.o(" FROM %s", statement.table.getSQLName());
        sc.o(" as %s", statement.table.alias);

        renderJoins(sc, statement);
        SqlStatement sqlStatement = new SqlStatement();
        renderWhere(sc, statement, sqlStatement);

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }


    private void renderWhere(StrCreator sc, LLD.LLSelect statement, SqlStatement sqlStatement) {
        if (statement.whereTok != null) {
//            MyWhereSqlVisitor visitor = new MyWhereSqlVisitor(factorySvc);
            NewWhereSqlVisitor visitor = new NewWhereSqlVisitor(factorySvc);
            //visitor.sc = new StrCreator(); //local one
            visitor.top = statement.whereTok.where;
            visitor.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            visitor.mainAlias = statement.table.alias;
            visitor.statement = statement;
            statement.whereTok.visit(visitor, null);
//            String str = visitor.sc.toString();
            String str = visitor.render();
            if (!str.isEmpty()) {
                sc.addStr(" WHERE ");
                sc.addStr(str);
            }

            SqlParamVisitor visitor2 = new SqlParamVisitor();
            visitor2.top = statement.whereTok.where;
            visitor2.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            statement.whereTok.visit(visitor2, null);
            int i = 0;
            for (DValue dval : visitor2.sqlParams) {
                Tok.ValueTok vexp = visitor2.fieldValues.get(i++);
                DValue realVal = sqlValueRenderer.preRenderSqlParam(dval, vexp.hintPair == null ? null : vexp.hintPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
            }
        }
    }

    private void renderJoins(StrCreator sc, LLD.LLSelect statement) {
        //SELECT a.id, a.firstName FROM a.customer as a JOIN address as b ON a.id=b.cust WHERE b.cust < 10");
        ListWalker<LLD.LLJoin> walker2 = new ListWalker<>(statement.joinL);
        while (walker2.hasNext()) {
            LLD.LLJoin join = walker2.next();
            if (join.logicalJoin.isTransitive) {
                String aliasRight = join.logicalJoin.alias;
                sc.o(" LEFT JOIN %s as %s ON %s.%s=%s.%s", join.physicalLeft.getSQLName(), aliasRight,
                        join.physicalRight.physicalTable.alias, join.physicalRight.physicalPair.name,
                        aliasRight, join.physicalLeft.physicalPair.name);
                walker2.addIfNotLast(sc, " ");
            } else {
                String aliasRight = join.logicalJoin.alias;
                sc.o(" LEFT JOIN %s as %s ON %s.%s=%s.%s", join.physicalRight.getSQLName(), aliasRight,
                        join.physicalLeft.physicalTable.alias, join.physicalLeft.physicalPair.name,
                        aliasRight, join.physicalRight.physicalPair.name);
                walker2.addIfNotLast(sc, " ");
            }
        }
    }

//    private boolean matchesField(Exp.FunctionExp distinctExp, LLD.LLField ff) {
//        if (distinctExp == null) return false;
//        return ff.getFieldName().equals(distinctExp.prefix);
//    }

    private boolean calcIfAreOrderableFields(LLD.LLSelect stmt) {
        List<LLD.LLDFuncEx> funcs = LLFieldHelper.extractFuncs(stmt.finalFieldsL);

        //functions like count can't use ORDER BY
        for (LLD.LLDFuncEx func : funcs) {
            //these fns are ok and still result in orderable fields being returned
            switch (func.fnName) {
                case "orderBy":
                case "fetch":
                case "offset":
                case "limit":
                    break;
                default: //anything else
                    return false;
            }
        }
        return true;
    }

    private LLD.LLDFuncEx getOrderByIfPresent(LLD.LLSelect stmt) {
        return LLFieldHelper.findFunc(stmt.finalFieldsL, "orderBy");
    }

    private void renderOrderBy(StrCreator sc, LLD.LLSelect statement, LLD.LLDFuncEx orderBy) {
        sc.o(" ORDER BY");
//        List<LLD.LLField> fields = statement.fields.stream().filter(x -> x instanceof LLD.LLField)
//                        .map(x -> (LLD.LLField) x).collect(Collectors.toList());

        List<LLD.LLFinalFieldEx> orderFields = orderBy.argsL.stream().filter(x -> x instanceof LLD.LLFinalFieldEx)
                .map(x -> (LLD.LLFinalFieldEx) x).collect(Collectors.toList());

        List<LLD.LLFuncArg> funcArgs = orderBy.argsL.stream().filter(x -> x instanceof LLD.LLFuncArg)
                .map(x -> (LLD.LLFuncArg) x).collect(Collectors.toList());
        Optional<String> ascStr = funcArgs.isEmpty() ? Optional.empty() : Optional.of(funcArgs.get(0).funcArg); //only use the first one

        //TODO Flight[true].wid.distinct().orderBy('id',desc) - id is from flight
        //Customer[true].addr.distinct().orderBy('id',desc) -here we orderby address.id if it exists else customer.id

        ListWalker<LLD.LLFinalFieldEx> walker2 = new ListWalker<>(orderFields);
        while (walker2.hasNext()) {
            LLD.LLFinalFieldEx el = walker2.next();
            String fieldName = el.fieldName;

            //TODO: Pass2Compiler should validate that orderBy.args are fields!
            String alias = el.physicalTable.alias; //findAlias(fieldName, statement, orderBy);
            sc.o(" %s.%s", alias, fieldName);
            walker2.addIfNotLast(sc, " ");
        }

        if (ascStr.isPresent()) {
            String asc = ascStr.get();
            sc.o(" %s", asc);
        }

    }

    String findAlias(String fieldName, LLD.LLSelect statement, LLD.LLDFuncEx orderBy) {
//        Optional<LLD.LLField> field = fields.stream().filter(x -> x.getFieldName().equals(fieldName)).findAny();
//        if (field.isPresent()) {
//            return field.get().physicalTable.alias;
//        }
        LLD.LLFinalFieldEx field = (LLD.LLFinalFieldEx) orderBy.argsL.get(0);
        return field.fieldName;
//
//        List<LLD.LLFinalFieldEx> finals = LLFieldHelper.extractFields(statement.finalFieldsL);
//        for(LLD.LLFinalFieldEx finalField: finals) {
//            if (finalField.fieldName.equals(fieldName)) {
//                return finalField.physicalTable.alias;
//            }
//        }
//
//        return statement.getTable().alias;
    }

}
