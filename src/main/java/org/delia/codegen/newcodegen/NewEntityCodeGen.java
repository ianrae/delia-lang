package org.delia.codegen.newcodegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.StringUtil;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class NewEntityCodeGen extends NewCodeGenBase {

	public NewEntityCodeGen() {
		super(true);
	}
	
	@Override
	public String buildJavaFileName(DType dtype) {
		String filename = String.format("%sEntity.java", dtype.getName());
		return filename;
	}

	@Override
	public String generate(DType typeParam) {
		DStructType structType = (DStructType) typeParam; //structTypesOnly is true
		String typeName = structType.getName();
		StrCreator sc = new StrCreator();
		helper().addImports(sc, structType);
		STGroup g = new STGroupFile("templates/entity.stg");
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());

//		String baseType = (structType.getBaseType() == null) ? "DeliaImmutable" : structType.getBaseType().getName();
		if (structType.getBaseType() == null) {
			//t2(cname,iname,ename,immutname) ::= <<
			st = g.getInstanceOf("t2");
			st.add("cname", typeName + "Entity");
			st.add("iname", typeName);
			st.add("ename", typeName + "Setter");
			st.add("immutname", typeName + "Immut");
		} else {
			st = g.getInstanceOf("t2base");
			st.add("cname", typeName + "Entity");
			st.add("base", structType.getBaseType().getName());
			st.add("iname", typeName);
			st.add("ename", typeName + "Setter");
			st.add("immutname", typeName + "Immut");
			
		}
		sc.o(st.render());
		sc.nl();

		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = helper().convertToJava(structType, fieldName);
			String javaObjType = helper().convertToJava(structType, fieldName, ftype, false);
			String asFn = helper().convertToAsFn(ftype);
			String nullValue = helper().getNullValueFor(structType, fieldName);

			if (ftype.isStructShape()) {
				//t4(ftype,fobjname,uname,fname) ::= <<
				st = g.getInstanceOf("t4");
				st.add("ftype", javaType);
				st.add("fobjname", javaObjType);
				st.add("uname", StringUtil.uppify(fieldName));
				st.add("fname", fieldName);
				st.add("nullval", "null");
				sc.o(st.render());
				
				if (helper().hasPK(ftype)) {
					//t5(ftype,uname,pktype,pkfield) ::= <<
					st = g.getInstanceOf("t5");
					st.add("ftype", javaType);
					st.add("uname", StringUtil.uppify(fieldName));
					st.add("pktype", helper().getPKType(ftype));
					st.add("pkfield", helper().getPKField(ftype));
					sc.o(st.render());
				}
			} else {
				//t3(ftype,fobjname,uname,fname,asname) ::= <<
				st = g.getInstanceOf("t3");
				st.add("ftype", javaType);
				st.add("fobjname", javaObjType);
				st.add("uname", StringUtil.uppify(fieldName));
				st.add("fname", fieldName);
				st.add("asname", asFn);
				st.add("nullval", nullValue);
				sc.o(st.render());
			}

			sc.nl();

		}
		sc.o("}");
		sc.nl();

		return sc.toString();
	}

	private void line(StrCreator sc, String str) {
		sc.o("%s\n", str);
	}

}