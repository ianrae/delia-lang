package org.delia.codegen.generators;

import org.delia.codegen.CodeGenBase;
import org.delia.db.sql.StrCreator;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.StringUtil;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class DaoBaseCodeGen extends CodeGenBase {

	private String entityPackageName;

	public DaoBaseCodeGen(String entityPackageName) {
		super(true);
		this.entityPackageName = entityPackageName;
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%sDaoBase.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();
		StrCreator sc = new StrCreator();
//		helper().addImports(sc, structType);
		addDoNotModifyComment(sc);
		sc.o("package %s;", packageName);
		sc.nl();
		
		
		STGroup g = new STGroupFile("templates/daoBase.stg");
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());
		sc.o("import %s.%s;\n", entityPackageName, typeName);
		sc.o("import %s.%sImmut;\n", entityPackageName, typeName);
		sc.o("import %s.%sEntity;\n", entityPackageName, typeName);
		sc.nl();

		//t2(cname,iname,bname,itname,immutname) ::= <<
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		if (pkpair == null) {
			st = g.getInstanceOf("t2");
			st.add("cname", typeName + "DaoBase");
			st.add("iname", typeName);
			st.add("itname", String.format("<%s>", typeName));
			st.add("immutname", typeName + "Immut");

			if (structType.getBaseType() == null) {
				st.add("bname", "EntityDaoBase");
			} else {
//				st.add("bname", structType.getBaseType().getName());
				st.add("bname", "EntityDaoBase");
			}
			sc.addStr(st.render());
			sc.nl();

			sc.o("}");
			sc.nl();
		} else {
			st = g.getInstanceOf("t2a");
			st.add("cname", typeName + "DaoBase");
			st.add("iname", typeName);
			st.add("itname", String.format("<%s>", typeName));
			st.add("immutname", typeName + "Immut");
			st.add("asFn", getPKType(structType));
			st.add("pkname", StringUtil.uppify(pkpair.name));

			if (structType.getBaseType() == null) {
				st.add("bname", "EntityDaoBase");
			} else {
//				st.add("bname", structType.getBaseType().getName());
				st.add("bname", "EntityDaoBase");
			}
			sc.addStr(st.render());
			sc.nl();

			sc.o("}");
			sc.nl();
		}

		return sc.toString();
	}

	private Object getPKType(DStructType structType) {
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		if (pkpair == null) {
			return null;
		}
		String s = BuiltInTypes.getDeliaTypeNameFromShape(pkpair.type.getShape());
		s = StringUtil.uppify(s);
		s = String.format("as%s", s);
		return s;
	}
}