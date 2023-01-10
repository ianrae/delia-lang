package org.delia.migrationparser.parser;

import org.delia.log.DeliaLog;
import org.delia.migrationparser.parser.ast.*;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StrCreator;

import java.util.ArrayList;
import java.util.List;

public class MigrationParser {

    private final DeliaLog log;

    public MigrationParser(DeliaLog log) {
        this.log = log;
    }

    public List<Token> parseIntoTokens(String migrationSrc) {
        Reader r = new Reader(migrationSrc);
        MLexer lexer = new MLexer(r);
        List<Token> tokens = new ArrayList<>();
        Token prev = null;
        while (true) {
            Token tok = lexer.getNextToken();
            if (tok == null) {
                return null;
            }
            if (tok.tokType == 99) {
                break;
            }
            tokens.add(tok);

            if (tok.tokType == MLexer.TOK_COLON) {
                if (prev != null && prev.tokType == MLexer.TOK_SYMBOL && prev.value.equals("ADDITIONS")) {
                    return tokens;
                }
            }
            prev = tok;
        }
        return tokens;
    }

    //TODO: add more error checking
    public List<AST> parseIntoAST(List<Token> tokens) {
        List<AST> list = new ArrayList<>();

        /*
            1 ALTERATIONS
            2 ADDITIONS
         */
        int state = 2;

        Token prev = null;
        String currentAction = null;
        List<String> pieces = new ArrayList<>();
        for (Token tok : tokens) {
            switch (tok.tokType) {
                case MLexer.TOK_SYMBOL:
                    if (currentAction == null) {
                        currentAction = tok.value;
                    } else {
                        pieces.add(tok.value);
                    }
                    break;
                case MLexer.TOK_INTEGER:
                case MLexer.TOK_LPAREN:
                case MLexer.TOK_RPAREN:
                        pieces.add(tok.value);
                    break;
                case MLexer.TOK_COLON:
                    if (isStr(currentAction, "ALTERATIONS")) {
                        state = 1;
                    } else if (isStr(currentAction, "ADDITIONS")) {
                        break;
                    }
                    break;
                case MLexer.TOK_COMMENT:
                case MLexer.TOK_END_LINE:
                    if (currentAction != null) {
                        buildAction(list, currentAction, pieces, state);
                    }
                    prev = null;
                    currentAction = null;
                    pieces = new ArrayList<>();
                    break;
                default:
                    break;
            }

            prev = tok;
        }

        if (currentAction != null) {
            buildAction(list, currentAction, pieces, state);
        }

        return list;
    }

    private void buildAction(List<AST> list, String currentAction, List<String> pieces, int state) {
        switch (currentAction) {
            case "ALTERATIONS":
            case "ADDITIONS":
                break;
            case "DROP":
                failIfNoteState1(state);
                list.add(new DropTypeAST(pieces.get(0)));
                break;
            case "RENAME":
                failIfNoteState1(state);
                list.add(new RenameTypeAST(pieces.get(0), pieces.get(2)));
                break;
            case "REMOVE":
                failIfNoteState1(state);
                list.add(new RemoveVarAST(pieces.get(0)));
                break;
            case "ALTER":
                failIfNoteState1(state);
                if (pieces.get(1).equals("DROP")) {
                    list.add(new DropFieldAST(pieces.get(0), pieces.get(2)));
                } else if (pieces.get(1).equals("RENAME")) {
                    list.add(new RenameFieldAST(pieces.get(0), pieces.get(2), pieces.get(4)));
                } else if (pieces.get(1).equals("ALTER")) {
                    String fieldName = extractFieldName(pieces);
                    list.add(new ChangeFieldAST(pieces.get(0), fieldName, pieces));
                } else if (pieces.get(1).equals("ADD")) {
                    String fieldName = extractFieldName(pieces);
                    list.add(new AddFieldAST(pieces.get(0), fieldName, pieces));
                }
                break;
            default:
                DeliaExceptionHelper.throwError("bad.migration.source", "bad migration source!");
        }
    }

    private String extractFieldName(List<String> pieces) {
        if (pieces.get(2).equals("relation")) {
            return pieces.get(3);
        } else {
            return pieces.get(2);
        }
    }

    private void failIfNoteState1(int state) {
        if (state != 1) {
            DeliaExceptionHelper.throwError("migration.missing.alterations", "Missing ALTERATIONS:");
        }
    }

    private boolean isStr(String currentAction, String s) {
        if (currentAction != null && currentAction.equals(s)) {
            return true;
        }
        return false;
    }

    public String findAdditions(String src) {
        String[] ar = splitIntoLines(src);
        StrCreator sc = null;
        for (String line : ar) {
            if (line.trim().equals("ADDITIONS:")) {
                sc = new StrCreator();
            } else if (sc != null) {
                sc.addStr(line);
                sc.nl();
                ;
            }
        }

        return sc == null ? src : sc.toString();
    }

    public String[] splitIntoLines(String s) {
        String[] ar = s.split(System.lineSeparator());
        return ar;
    }

}
