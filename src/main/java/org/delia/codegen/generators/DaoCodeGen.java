package org.delia.codegen.generators;

import org.delia.codegen.CodeGenBase;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class DaoCodeGen extends CodeGenBase {

	private String entityPackageName;
	private String baseDaoPackageName;

	public DaoCodeGen(String entityPackageName, String baseDaoPackageName) {
		super(true);
		this.entityPackageName = entityPackageName;
		this.baseDaoPackageName = baseDaoPackageName;
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%sDao.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();
		StrCreator sc = new StrCreator();
		sc.o("package %s;", packageName);
		sc.nl();
		
		
		STGroup g = loadTemplate();
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());
		sc.o("import %s.%s;\n", entityPackageName, typeName);
		sc.o("import %s.%sImmut;\n", entityPackageName, typeName);
		sc.o("import %s.%sDaoBase;\n", baseDaoPackageName, typeName);
		sc.nl();

		//t2(cname,iname,bname,itname,immutname) ::= <<
		st = g.getInstanceOf("t2");
		st.add("cname", typeName + "Dao");
		st.add("iname", typeName);
		st.add("itname", String.format("<%s>", typeName));
		st.add("immutname", typeName + "Immut");
		st.add("bname", String.format("%sDaoBase", typeName));

		sc.addStr(st.render());
		sc.nl();

		sc.o("}");
		sc.nl();

		return sc.toString();
	}
	
	/**
	 * Override this to use your own template file.
	 * @return stringtemplate STGroup
	 */
	protected STGroup loadTemplate() {
		STGroup g = new STGroupFile("templates/dao.stg");
		return g;
	}
	
}