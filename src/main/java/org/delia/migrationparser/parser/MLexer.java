package org.delia.migrationparser.parser;


public class MLexer {
    public static final int TOK_SYMBOL = 2;
    public static final int TOK_INTEGER = 3;
//    public static final int TOK_BOOLEAN = 10;
    public static final int TOK_STRING_LITERAL = 11;

    public static final int TOK_END_LINE = 20;
    public static final int TOK_COMMENT = 21;
    public static final int TOK_COLON = 22;
    public static final int TOK_LPAREN = 23;
    public static final int TOK_RPAREN = 24;

    private final Reader r;

    public MLexer(Reader r) {
        this.r = r;
    }

    public Token getNextToken() {
        if (r.isEof()) {
            return new Token(99, null);
        }
        char ch = r.peek();

        while (Character.isWhitespace(ch)) {
            if (ch == '\r' || ch == '\n') {
                break;
            }
            r.consume();
            if (r.isEof()) {
                return new Token(99, null);
            }
            ch = r.peek();
        }

        if (ch == '\r') {
            r.consume();
            ch = r.peek();
            if (ch == '\n') {
                r.consume();
                return new Token(TOK_END_LINE, null);
            }
        } else if (ch == '\n') {
            r.consume();
            return new Token(TOK_END_LINE, null);
        } else if (ch == '/' && r.peek(1) == '/') {
            r.consume();
            r.consume();
            while (true) {
                if (r.isEof()) {
                    return new Token(99, null);
                }

                if (r.nextIs('\n')) {
                    return new Token(TOK_COMMENT, null);
                }
                r.consume();
            }
        } else if (ch == ':') {
            r.consume();
            return new Token(TOK_COLON, ":");
        } else if (ch == '(') {
            r.consume();
            return new Token(TOK_LPAREN, "(");
        } else if (ch == ')') {
            r.consume();
            return new Token(TOK_RPAREN, ")");
        }


        boolean isUnderscore = ch == '_';
        if (isUnderscore || Character.isAlphabetic(ch)) {
            return readSymbol(ch);
        } else if (Character.isDigit(ch)) {
            return readInteger(ch);
        } else if (ch == '\'' || ch == '"') {
            return readStringLiteral(ch);
        }

        return null;
    }

    private Token readSymbol(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append(ch);
        r.consume();
        while (!r.isEof() && (Character.isDigit(r.peek()) || Character.isAlphabetic(r.peek()))) {
            ch = r.consume();
            sb.append(ch);
        }

        String str = sb.toString();
//        if (str.equals("false") || str.equals("true")) {
//            return new Token(TOK_BOOLEAN, str);
//        }

        return new Token(TOK_SYMBOL, str);
    }
    private Token readInteger(char ch) {
        StringBuilder sb = new StringBuilder();
        sb.append(ch);
        r.consume();
        while (!r.isEof() && (Character.isDigit(r.peek()))) {
            ch = r.consume();
            sb.append(ch);
        }

        String str = sb.toString();
//        if (str.equals("false") || str.equals("true")) {
//            return new Token(TOK_BOOLEAN, str);
//        }

        return new Token(TOK_INTEGER, str);
    }


    private Token readStringLiteral(char startDelim) {
        StringBuilder sb = new StringBuilder();
        r.consume();
        while (!r.isEof() && !r.nextIs(startDelim)) {
            char ch = r.consume();
            sb.append(ch);
        }
        if (!r.isEof()) {
            r.consume(); //final delim
        }

        return new Token(TOK_STRING_LITERAL, sb.toString());
    }
}
