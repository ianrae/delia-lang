package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class NewDaoBaseCodeGen extends NewCodeGenBase {

	public NewDaoBaseCodeGen() {
		super(true);
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
		helper().addImports(sc, structType);
		STGroup g = new STGroupFile("templates/daoBase.stg");
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());

//		String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
		if (structType.getBaseType() == null) {
			st = g.getInstanceOf("t2");
			st.add("cname", typeName + "Entity");
			st.add("iname", typeName);
			st.add("bname", "EntityDaoBase");
			st.add("ename", typeName + "Setter");
			st.add("immutname", typeName + "Immut");
		} else {
			st = g.getInstanceOf("t2base");
			st.add("cname", typeName + "Entity");
			st.add("bname", structType.getBaseType().getName());
			st.add("iname", typeName);
			st.add("ename", typeName + "Setter");
			st.add("immutname", typeName + "Immut");
			
		}
		sc.o(st.render());
		sc.nl();

		sc.o("}");
		sc.nl();

		return sc.toString();
	}
}