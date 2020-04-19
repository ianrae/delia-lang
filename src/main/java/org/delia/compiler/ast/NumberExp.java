package org.delia.compiler.ast;

public class NumberExp extends ExpBase implements ValueExp {
    public Double val;

    public NumberExp(int pos, Double s) {
		super(pos);
        this.val = s;
    }
    public NumberExp(Double s) {
		super(99);
        this.val = s;
    }
    @Override
    public String strValue() {
        return val.toString();
    }
}