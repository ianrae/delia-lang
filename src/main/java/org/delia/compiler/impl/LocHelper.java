package org.delia.compiler.impl;

import org.antlr.v4.runtime.Token;
import org.delia.compiler.ast.AST;

public class LocHelper {

    public static AST.Loc genLoc(Token tokStart, Token tokEnd) {
        AST.Loc loc = new AST.Loc();
        loc.lineNum = tokStart.getLine(); //1..n
        loc.charOffset = tokStart.getCharPositionInLine();
        loc.length = tokEnd.getStopIndex() - tokStart.getStartIndex();
        return loc;
    }
}
