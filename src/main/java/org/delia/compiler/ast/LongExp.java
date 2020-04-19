package org.delia.compiler.ast;

public class LongExp extends ExpBase implements ValueExp {
    public Long val;

    public LongExp(int pos, Long s) {
		super(pos);
        this.val =s;
    }
    public LongExp(Long s) {
		super(99);
        this.val =s;
    }
    @Override
    public String strValue() {
        return val.toString();
    }
}