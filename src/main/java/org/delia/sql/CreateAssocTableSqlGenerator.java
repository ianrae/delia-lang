package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.AssocSpec;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DTypeNameUtil;
import org.delia.util.DValueHelper;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.Locale;

public class CreateAssocTableSqlGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;
    private final DatService datSvc;
    private final SqlTypeConverter sqlTypeConverter;
    private final SqlTableNameMapper sqlTableNameMapper;

    public CreateAssocTableSqlGenerator(FactoryService factorySvc, SqlValueRenderer sqlValueRenderer, ScalarValueBuilder valueBuilder,
                                        DatService datSvc, DeliaOptions deliaOptions, SqlTableNameMapper sqlTableNameMapper) {
        super(factorySvc);
        this.sqlValueRenderer = sqlValueRenderer;
        this.valueBuilder = valueBuilder;
        this.datSvc = datSvc;
        this.sqlTypeConverter = new SqlTypeConverter(deliaOptions);
        this.sqlTableNameMapper = sqlTableNameMapper;
    }

    public SqlStatement render(LLD.LLCreateAssocTable statement) {
        StrCreator sc = new StrCreator();
        sc.o("CREATE TABLE IF NOT EXISTS %s (", statement.getTableName());
        sc.nl();

        AssocSpec spec = statement.assocSpec;
        String field = spec.leftColumn;
        TypePair pkpair1 = DValueHelper.findPrimaryKeyFieldPair(spec.leftType);
        String sqlType = sqlTypeConverter.getSqlType(pkpair1.type);

        boolean optional = spec.rightType.fieldIsOptional(spec.otherSideFieldName); //leftv holds Customer so look at Address.cust
        if (optional) {
            sc.o(" %s %s,", field, sqlType);
        } else {
            sc.o(" %s %s NOT NULL,", field, sqlType);
        }
        sc.nl();

        field = spec.rightColumn;
        TypePair pkpair2 = DValueHelper.findPrimaryKeyFieldPair(spec.rightType);
        sqlType = sqlTypeConverter.getSqlType(pkpair2.type);
        optional = spec.leftType.fieldIsOptional(spec.deliaLeftv);
        if (optional) {
            sc.o(" %s %s,", field, sqlType);
        } else {
            sc.o(" %s %s NOT NULL,", field, sqlType);
        }
        sc.nl();

        //constraints
        addAssocConstraint(sc, spec.leftColumn, statement, spec.leftType, pkpair1, ",");
        addAssocConstraint(sc, spec.rightColumn, statement, spec.rightType, pkpair2, "");

        sc.o(");");

        SqlStatement sqlStatement = new SqlStatement();
        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    private void addAssocConstraint(StrCreator sc, String field, LLD.LLCreateAssocTable statement, DStructType leftType, TypePair pkpair, String extra) {
        String constraintName = String.format("FK_%s_%s", statement.getTableName(), field).toUpperCase(Locale.ROOT);
        String tblName = sqlTableNameMapper.calcSqlTableName(leftType);
        sc.o(" CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)%s", constraintName, field,
                tblName, pkpair.name, extra);
        sc.nl();
    }

}
