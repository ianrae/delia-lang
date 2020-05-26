package org.delia.codegen;

import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.StringUtil;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class ImmutCodeGen extends CodeGenBase {

	public ImmutCodeGen(DTypeRegistry registry, String packageName) {
		super(registry, packageName);
	}

	public String generate(DStructType structType) {
		String typeName = structType.getName();
		StrCreator sc = new StrCreator();
		addImports(sc, structType);
		
		STGroup g = new STGroupFile("templates/immut.stg");
		//t1() ::= <<
		ST st = g.getInstanceOf("t1");
		sc.o(st.render());
		
		st = g.getInstanceOf("t2");
		st.add("cname", typeName + "Immut");
		st.add("iname", typeName);
		
		sc.o(st.render());
		sc.nl();
		
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = convertToJava(structType, fieldName);
			String asFn = convertToAsFn(ftype);
			
			if (ftype.isStructShape()) {
				//t4(ftype,uname,fname) ::= <<
				st = g.getInstanceOf("t4");
				st.add("ftype", javaType);
				st.add("uname", StringUtil.uppify(fieldName));
				st.add("fname", fieldName);
				sc.o(st.render());
				
				if (hasPK(ftype)) {
					//t5(ftype,uname,fname,pktype,asname) ::= <<
					st = g.getInstanceOf("t5");
					st.add("ftype", javaType);
					st.add("uname", StringUtil.uppify(fieldName));
					st.add("fname", fieldName);
					st.add("pktype", getPKType(ftype));
					st.add("asname", getPKTypeAsFn(ftype));
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

	private void line(StrCreator sc, String str) {
		sc.o("%s\n", str);
	}

}