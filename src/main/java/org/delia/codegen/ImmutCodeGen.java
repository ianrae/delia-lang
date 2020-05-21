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

	public ImmutCodeGen(DTypeRegistry registry) {
		super(registry);
	}

	public String generate(DStructType structType) {
		String typeName = structType.getName();
		
		STGroup g = new STGroupFile("templates/immut.stg");
		ST st = g.getInstanceOf("t2");
		st.add("cname", typeName + "Immut");
		st.add("iname", typeName);
		
		StrCreator sc = new StrCreator();
		sc.o(st.render());
		sc.nl();
		
		for(String fieldName: structType.getDeclaredFields().keySet()) {
			DType ftype = structType.getDeclaredFields().get(fieldName);

			String javaType = convertToJava(ftype);
			String asFn = convertToAsFn(ftype);
			
			//t3(ftype,uname,fname,asname) ::= <<
			st = g.getInstanceOf("t3");
			st.add("ftype", javaType);
			st.add("uname", StringUtil.uppify(fieldName));
			st.add("fname", fieldName);
			st.add("asname", asFn);
			sc.o(st.render());
			
			sc.nl();

		}
		sc.o("}");
		sc.nl();

		return sc.str;
	}

	private void line(StrCreator sc, String str) {
		sc.o("%s\n", str);
	}

}