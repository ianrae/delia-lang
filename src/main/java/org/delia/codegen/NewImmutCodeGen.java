package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.StringUtil;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class NewImmutCodeGen extends NewCodeGenBase {

	public NewImmutCodeGen() {
		super(true);
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%sImmut.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();
		StrCreator sc = new StrCreator();
		helper().addImports(sc, structType);
		
		STGroup g = new STGroupFile("templates/immut.stg");
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());
		
//		String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
		if (structType.getBaseType() == null) {
			st = g.getInstanceOf("t2");
			st.add("cname", typeName + "Immut");
			st.add("iname", typeName);
		} else {
			st = g.getInstanceOf("t2base");
			st.add("cname", typeName + "Immut");
			st.add("base", structType.getBaseType().getName());
			st.add("iname", typeName);
		}
		
		sc.o(st.render());
		sc.nl();
		
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = helper().convertToJava(structType, fieldName);
			String asFn = helper().convertToAsFn(ftype);
			
			if (ftype.isStructShape()) {
				//t4(ftype,uname,fname) ::= <<
				st = g.getInstanceOf("t4");
				st.add("ftype", javaType);
				st.add("uname", StringUtil.uppify(fieldName));
				st.add("fname", fieldName);
				sc.o(st.render());
				
				if (helper().hasPK(ftype)) {
					//t5(ftype,uname,fname,pktype,asname) ::= <<
					st = g.getInstanceOf("t5");
					st.add("ftype", javaType);
					st.add("uname", StringUtil.uppify(fieldName));
					st.add("fname", fieldName);
					st.add("pktype", helper().getPKType(ftype));
					st.add("asname", helper().getPKTypeAsFn(ftype));
					sc.o(st.render());
				}
			} else {
				//t3(ftype,uname,fname,asname) ::= <<
				st = g.getInstanceOf("t3");
				st.add("ftype", javaType);
				st.add("uname", StringUtil.uppify(fieldName));
				st.add("fname", fieldName);
				st.add("asname", asFn);
				sc.o(st.render());
			}
			
			sc.nl();

		}
		sc.o("}");
		sc.nl();

		return sc.toString();
	}
}