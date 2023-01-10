package org.delia.compiler.impl;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.delia.compiler.ast.AST;

public class DeliaParseException extends ParseCancellationException {
    public AST.Loc loc;

    public DeliaParseException(int lineNum, int charOffset, String msg) {
        super(msg);
        loc = new AST.Loc();
        loc.lineNum = lineNum;
        loc.charOffset = charOffset;
        loc.length = 0; //??
    }
}
