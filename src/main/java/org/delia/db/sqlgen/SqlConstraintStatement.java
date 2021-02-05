package org.delia.db.sqlgen;

import org.delia.db.SqlStatement;
import org.delia.db.schema.modify.SchemaChangeOperation;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StringUtil;

public class SqlConstraintStatement implements SqlStatementGenerator {

	private SchemaChangeOperation op;

	public SqlConstraintStatement() {
	}

	public void init(SchemaChangeOperation op) {
		this.op = op;
	}

	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(this);
		StrCreator sc = new StrCreator();
		switch(op.opType) {
		case CONSTRAINT_ADD:
			renderAdd(sc);
			break;
		case CONSTRAINT_DELETE:
			renderDelete(sc);
			break;
		case CONSTRAINT_ALTER:
			renderUpdate(sc);
			break;
		default:
			DeliaExceptionHelper.throwNotImplementedError("constraint");
			break;
		}

		stm.sql = sc.toString();
		return stm;
	}

	private void renderUpdate(StrCreator sc) {
		//uniqueFields can't really be altered.just drop and add
		StrCreator sc1 = new StrCreator();
		renderDelete(sc1);
		sc.addStr(sc1.toString());
		sc.addStr("; ");

		sc1 = new StrCreator();
		renderAdd(sc1);
		sc.addStr(sc1.toString());
	}

	private void renderAdd(StrCreator sc) {
		sc.o("ALTER TABLE %s", op.typeName);
		//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
		sc.addStr(" ADD CONSTRAINT");
		String s = StringUtil.flattenEx(op.argsL, "_");
		String name = String.format("%s_%s__%s", op.typeName, op.otherName, s);
		sc.o(" %s", name);
		sc.o(" UNIQUE(");
		
		ListWalker<String> walker = new ListWalker<>(op.argsL);
		while(walker.hasNext()) {
			String ff = walker.next();
			sc.o(ff);
			walker.addIfNotLast(sc, ", ");
		}
		sc.addStr(")");
	}

	private void renderDelete(StrCreator sc) {
		sc.o("ALTER TABLE %s", op.typeName);
		//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
		sc.addStr(" DROP CONSTRAINT");
		String s = StringUtil.flattenEx(op.argsL, "_");
		String name = String.format("%s_%s__%s", op.typeName, op.otherName, s);
		sc.o(" %s", name);
	}
}
