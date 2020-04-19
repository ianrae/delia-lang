package org.delia.compiler.generate;

import java.util.Stack;

public class GeneratorContext {
	public final static String LIST = "L";
	public final static String STRUCT = "S";
	public final static String MAP = "M";
	
    private Stack<String> shapeStack = new Stack<>();
    public boolean expandSubOjectsFlag = true;
    public int indentLevel = 0;
    
    public void pushShapeCode(String shape) {
    	shapeStack.push(shape);
    }
    public void popShapeCode() {
    	shapeStack.pop();
    }
    public String getCurrentShapeCode() {
        String shape = (shapeStack.isEmpty()) ? "" : shapeStack.peek();
        return shape;
    }
    public boolean isEquals(String shapeCode) {
    	String shape = getCurrentShapeCode();
    	return shape.equals(shapeCode);
    }
}