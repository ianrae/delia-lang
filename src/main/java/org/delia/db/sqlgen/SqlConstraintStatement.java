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
		case INDEX_ADD:
			renderAddIndex(sc);
			break;
		case INDEX_DELETE:
			renderDeleteIndex(sc);
			break;
		case INDEX_ALTER:
			renderUpdateIndex(sc);
			break;
		default:
			DeliaExceptionHelper.throwNotImplementedError("constraint");
			break;
		}

		stm.sql = sc.toString();
		return stm;
	}

	private void renderAdd(StrCreator sc) {
		sc.o("ALTER TABLE %s", op.typeName);
		//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
		sc.addStr(" ADD CONSTRAINT");
		String name = makeName("");
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
	private void renderDelete(StrCreator sc) {
		sc.o("ALTER TABLE %s", op.typeName);
		//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
		sc.addStr(" DROP CONSTRAINT");
		String name = makeName("");
		sc.o(" %s", name);
	}
	
	// --- index ---
	private void renderAddIndex(StrCreator sc) {
		String name = makeName("idx");
		sc.o("CREATE INDEX %s", name);
		//CREATE INDEX IDXNAME ON TEST(NAME)
		sc.o(" ON %s(", op.typeName);
		
		ListWalker<String> walker = new ListWalker<>(op.argsL);
		while(walker.hasNext()) {
			String ff = walker.next();
			sc.o(ff);
			walker.addIfNotLast(sc, ", ");
		}
		sc.addStr(")");
	}
	private void renderUpdateIndex(StrCreator sc) {
		//ALTER INDEX IDXNAME RENAME TO IDX_TEST_NAME
		String name = makeName("idx");
		sc.o("ALTER INDEX %s", name);
		//CREATE INDEX IDXNAME ON TEST(NAME)
		sc.o(" RENAME TO %s", "KKKKKKKKKKKKK"); //TODO FIX
	}
	private void renderDeleteIndex(StrCreator sc) {
		String name = makeName("idx");
		sc.o("DROP INDEX %s", name);
		//DROP INDEX IF EXISTS IDXNAME
	}

	
	// -- helpers
	private String makeName(String prefix) {
		String s = StringUtil.flattenEx(op.argsL, "_");
		String name = String.format("%s%s_%s__%s", prefix, op.typeName, op.otherName, s);
		return name;
	}

	
}
