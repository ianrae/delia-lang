package org.delia.compiler.generate;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.*;

/**
 * Converter for converting a DValue type into some other format, such as JSON.
 * Uses a ValueGenerator in a visitor pattern.
 * 
 * @author Ian Rae
 *
 */
public class DeliaGeneratePhase extends ServiceBase {
	    private DTypeRegistry registry;
//	    private LineLocator lineLocator;

	    public DeliaGeneratePhase(FactoryService factorySvc, DTypeRegistry registry) {
	        super(factorySvc);
	        this.registry = registry;
//	        this.lineLocator = lineLocator;
	    }
	    
//	    public boolean generateTypes(TypeGenerator visitor) {
//	        boolean b = false;
//	        try {
//	            b = doGenerateTypes(visitor);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	        return b;
//	    }
//	    public boolean generateValues(ValueGenerator visitor) {
//	        boolean b = false;
//	        try {
//	            b = doGenerateValues(visitor);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	        return b;
//	    }
	    public boolean generateValue(ValueGenerator visitor, DValue dval, String varName) {
	        boolean b = false;
	        try {
	            b = doGenerateValue(visitor, dval, varName);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return b;
	    }

//	    private boolean doGenerateTypes(TypeGenerator visitor) throws Exception {
//	        List<DType> orderedTypeList = registry.getOrderedList();
//
//	        for(DType dtype: orderedTypeList) {
//	        	if (TypeInfo.isBuiltIntype(dtype.getName())) {
//	        		continue;
//	        	}
//
//	        	if (dtype.isStructShape()) {
//	        		DStructType fste = (DStructType) dtype;
//	        		String typeName = TypeInfo.parserTypeOf(fste.getCompleteName());
//	        		String parentName = (fste.getBaseType() == null) ? "struct" : fste.getBaseType().getCompleteName();
//	        		visitor.structType(fste, typeName, parentName);
//	        	} else if (dtype.isShape(Shape.ENUM)) {  
//	        		DStructType structType = (DStructType) dtype;
//	        		String typeName = TypeInfo.parserTypeOf(structType.getCompleteName());
//	        		visitor.enumType(structType, typeName);
//	        	} else if (dtype instanceof DListType) {
//	        		DListType listType = (DListType) dtype;
//	        		String typeName = TypeInfo.parserTypeOf(listType.getCompleteName());
//	        		String elementName = TypeInfo.parserTypeOf(listType.getElementType().getCompleteName());
//	        		visitor.listType(listType, typeName, elementName);
//	        	} else if (dtype instanceof DMapType) {
//	        		DMapType mapType = (DMapType) dtype;
//	        		String typeName = TypeInfo.parserTypeOf(mapType.getCompleteName());
//	        		String elementName = TypeInfo.parserTypeOf(mapType.getElementType().getCompleteName());
//	        		visitor.mapType(mapType, typeName, elementName);
//	        	} else {
//	        		String typeName = TypeInfo.parserTypeOf(dtype.getCompleteName());
//	        		String parentName = TypeInfo.parserTypeOf(dtype.getBaseType().getCompleteName());
//	        		visitor.scalarType(dtype, typeName, parentName);
//	        	}
//	        }
//	        
//	        if (! visitor.finish()) {
//	        	this.addError("type-visitor finish() failed", new StringExp(""));
//	        }
//
//	        return areNoErrors();
//	    }
	    
//	    private boolean doGenerateValues(ValueGenerator visitor) throws Exception {
//	        List<String> orderedValueList = world.getOrderedList();
//	        for(String valueName: orderedValueList) {
//	        	DValue dval = world.findTopLevelValue(valueName);
//	        	doval(visitor, valueName, dval, null, new GeneratorContext(), 0);
//	        }
//
//	        if (! visitor.finish()) {
//	        	this.addError("value-visitor finish() failed", new StringExp(""));
//	        }
//
//	        return areNoErrors();
//	    }
	    
	    private boolean doGenerateValue(ValueGenerator visitor, DValue dval, String varName) throws Exception {
        	doval(visitor, varName, dval, null, new GeneratorContext(), 0);

	        if (! visitor.finish()) {
	        	et.add("value-visitor-finish-fail", "value-visitor finish() failed");
	        }

	        return areNoErrors();
	    }

	    private boolean areNoErrors() {
	    	return this.et.areNoErrors();
		}

		private void doval(ValueGenerator visitor, String varName, DValue dval, String name, GeneratorContext genctx, int indexParam) throws Exception {

	        if (dval == null) {
	            //optional field
	        	if (genctx.isEquals(GeneratorContext.STRUCT)) {
	        		visitor.structMemberValue(name, dval, genctx, indexParam);
	        	} else {
	        		visitor.scalarValue(varName, dval, genctx);
	        	}
	        } else if (dval.getType().isStructShape()) {
	        	DStructType structType = (DStructType) dval.getType();
	        	ValuePlacement placement = new ValuePlacement(varName, name);
	        	visitor.startStruct(placement, dval, structType, genctx, indexParam);
	        	
	        	genctx.pushShapeCode(GeneratorContext.STRUCT);
	            DStructHelper helper = dval.asStruct();

	            int index = 0;
	            for(TypePair pair : structType.getAllFields()) {
	            	String fieldName = pair.name;
	                DValue inner = helper.getField(fieldName);
	                doval(visitor, null, inner, fieldName, genctx, index); //!recursion!
	                
	                if (inner != null && inner.getType().isRelationShape()) {
	                	doFetchedValues(visitor, inner, genctx);  //!recursion!
	                }
	                
	                index++;
	            }
	            genctx.popShapeCode();
	            visitor.endStruct(placement, dval, structType, genctx);
	        } else {
	        	if (genctx.isEquals(GeneratorContext.STRUCT)) {
	        		visitor.structMemberValue(name, dval, genctx, indexParam);
	        	} else {
	        		visitor.scalarValue(varName, dval, genctx);
	        	}
	        }
	    }

		private void doFetchedValues(ValueGenerator visitor, DValue inner, GeneratorContext genctx) throws Exception {
			if (! genctx.expandSubOjectsFlag) {
				return;
			}
			genctx.expandSubOjectsFlag = false; //protect against infinite recursion
			genctx.indentLevel = 1;
			DRelation drel = inner.asRelation();
			if (!drel.haveFetched()) {
				return;
			}
			for(DValue dval: drel.getFetchedItems()) {
                doval(visitor, null, dval, null, genctx, 0); //!recursion!
			}
			genctx.expandSubOjectsFlag = true;
			genctx.indentLevel = 0;
			visitor.endSubValue(genctx);
		}
	}