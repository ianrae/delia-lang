package org.delia.compiler.generate;

import org.delia.log.LoggableBlob;
import org.delia.type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Converts a DValue into a very simple text format. Used by unit tests.
 * @author Ian Rae
 *
 */
public class SimpleFormatOutputGenerator implements ValueGenerator {
	public boolean includeVPrefix = true;
	public boolean truncateLargeBlob = false;
	
	public List<String> outputL = new ArrayList<>();

	// -- types --

	// -- values --
	@Override
	public void startStruct(ValuePlacement placement, DValue dval, DStructType structType, GeneratorContext genctx, int index) {
		if (placement.isTopLevelValue) {
			String typeName = getTypeName(dval.getType());
			String s = String.format("value:%s:%s {", placement.name, typeName);
			write(s, genctx);
		} else if (placement.name == null) {
			String s = String.format(" {");
			write(s, genctx);
		} else {
//			String s = String.format(" v%s {", placement.name);
			String typeName = getTypeName(dval.getType());
			String s = String.format("value:%s:%s {", placement.name, typeName);
			write(s, genctx);
		}
	}

	@Override
	public void endStruct(ValuePlacement placement, DValue dval, DStructType structType, GeneratorContext genctx) {
		write("}", genctx);
	}

	@Override
	public void structMemberValue(String fieldName, DValue dval, GeneratorContext genctx, int index) {
		String value = DValToString(dval, genctx);
		String s;
		if (includeVPrefix) {
			s = String.format(" v%s:%s", fieldName, value);
		} else {
			s = String.format(" %s:%s", fieldName, value);
		}
		write(s, genctx);
	}

	@Override
	public void scalarValue(String varName, DValue dval, GeneratorContext genctx) {
		if (varName != null) {
			if (dval == null) {
				write("null", genctx);
			} else {
				String typeName = getTypeName(dval.getType());
				String value = DValToString(dval, genctx);
				String s = String.format("value:%s:%s:%s", varName, typeName, value);
				write(s, genctx);
			}
		}
	}

	//--helpers--
	private void addRules(DType dtype) {
		//FUTURE: add later!
//        for(NRule rule: dtype.getRawRules()) {
//            String ruleText = rule.getRuleText();
//            if (rule instanceof UniqueRule) {
//            	ruleText= String.format("unique %s", ruleText); 
//            }
//            outputL.add(" r: " + ruleText); 
//        }
	}
	private String getTypeName(DType dtype) {
		String typeName = dtype.getName();
		typeName = BuiltInTypes.convertDTypeNameToDeliaName(typeName);
		return typeName;
	}

	private String genIndent(int amount) {
		String space = "";
		for(int i = 0; i < amount; i++) {
			space += " ";
		}
		return space;
	}
    private String DValToString(DValue dval, GeneratorContext genctx) {
        if (dval == null) {
            return "null";
        } else if (dval.getType().isShape(Shape.RELATION)) {
        	DRelation rel = dval.asRelation();
        	if (rel.isMultipleKey()) {
        		return buildMultipleRef(rel, genctx);
        	}
        	DValue fkval = rel.getMultipleKeys().isEmpty() ? null : rel.getMultipleKeys().get(0);
        	String keyStr = DValToString(fkval, genctx); //*** recursion ***
			if ("null".equals(keyStr)) {
				return keyStr;
			}
        	boolean b = genctx.expandSubOjectsFlag ? rel.haveFetched() : false;
        	String suffix = b ? ":" : "}";
        	return String.format("{%s%s", keyStr, suffix);
        } else if (dval.getType().isShape(Shape.STRING)) {
        	String s = dval.asString();
        	return String.format("'%s'", s);
        } else if (dval.getType().isBlobShape()) {
        	//TODO: add config param so can output all or a reasonable subset of blob
        	if (truncateLargeBlob) {
        		WrappedBlob wblob = dval.asBlob();
        		LoggableBlob lb = new LoggableBlob(wblob.getByteArray());
        		return lb.toString();
        	}
        }
        
        return dval.asString();
    }
	private String buildMultipleRef(DRelation rel, GeneratorContext genctx) {
		StringJoiner joiner = new StringJoiner(",");
		for(DValue key: rel.getMultipleKeys()) {
			String keyStr = DValToString(key, genctx); //*** recursion ***
			joiner.add(keyStr);
		}
		return String.format("{[%s]}", joiner.toString());
	}

	@Override
	public boolean finish() {
		return true;
	}

	private String createIndent(GeneratorContext genctx) {
		int len = genctx.indentLevel * 2;
		char[] charArray = new char[len];
		Arrays.fill(charArray, ' ');
		String str = new String(charArray);		
		return str;
	}

	private void write(String s, GeneratorContext genctx) {
		String indent = createIndent(genctx);
		outputL.add(indent + s);
	}

	@Override
	public void endSubValue(GeneratorContext genctx) {
		write("}", genctx);
	}
}