package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.tok.TokVisitorUtils;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StrCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class NewWhereSqlVisitor implements Tok.TokVisitor {
    public static class StrFragment implements Tok.TokBase {
        public String str;

        public StrFragment(String str) {
            this.str = str;
        }

        @Override
        public void visit(Tok.TokVisitor visitor, Tok.TokBase parent) {
        }
    }

    private final FactoryService factorySvc;
    private final SqlValueRenderer sqlValueRenderer;
    public TypePair pkpair;
    public String mainAlias;
    public Tok.OperandTok top;
    public LLD.LLSelect statement;

    public List<Tok.TokBase> rawList = new ArrayList<>();

    public NewWhereSqlVisitor(FactoryService factorySvc) {
        this.factorySvc = factorySvc;
        this.sqlValueRenderer = new SqlValueRenderer(factorySvc);
    }

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (parent == null && exp instanceof Tok.DottedTok) {
            //ignore
        } else {
            rawList.add(exp);
        }
    }

    public String render() {
        StrCreator sc = new StrCreator();
        for (Tok.TokBase exp : rawList) {
            if (exp instanceof Tok.ValueTok) {
                Tok.ValueTok vexp = (Tok.ValueTok) exp;
                if (vexp.strValue().equals("true")) {
                    return ""; //done
                }
            } else if (exp instanceof Tok.OperandTok) {
                if (exp instanceof Tok.PKWhereTok) {
                    Tok.PKWhereTok pktok = (Tok.PKWhereTok) exp;
                    String tmp = "?"; //sqlValueRenderer.renderAsSql(vexp.value, pkpair.type, null);
                    String alias = pktok.alias != null ? pktok.alias : mainAlias;
                    if (pktok.primaryKey != null && pktok.isCompositeKey()) {
                        int index = 0;
                        for(TypePair pair: pktok.primaryKey.getKeys()) {
                            String fieldName = pair.name;
                            if (index > 0) {
                                sc.addStr(" and ");
                            }
                            sc.o("%s = %s", genField(alias, fieldName), tmp);
                            index++;
                        }
                        return sc.toString();

                    } else {
                        String fieldName = pktok.physicalFieldName != null ? pktok.physicalFieldName : pkpair.name;
                        sc.o("%s = %s", genField(alias, fieldName), tmp);
                        return sc.toString();
                    }
                }

                Tok.ValueTok vexp = TokVisitorUtils.getSingleChainValue((Tok.OperandTok) exp);
                if (vexp != null) {
                    if (vexp.strValue().equals("true")) {
                        return ""; //done
                    }
//                    String tmp = "?"; //sqlValueRenderer.renderAsSql(vexp.value, pkpair.type, null);
//
////                    String alias = vexp.pkOwnerAlias != null ? vexp.pkOwnerAlias : mainAlias;
//                    String alias = mainAlias;
//                    sc.o("%s = %s", genField(alias, pkpair.name), tmp);
//                    return sc.toString();
                }
            }
            break; //go on to next phase
        }

        //if we're here then its a full expression
        List<Tok.TokBase> currentList = rawList;
        for (int runawayCounter = 0; runawayCounter < 100; runawayCounter++) {
            List<Tok.TokBase> list2 = runOneLoop(currentList);
            if (list2.size() == 1 && list2.get(0) instanceof StrFragment) {
                return ((StrFragment) list2.get(0)).str;
            }
            currentList = list2;
        }
        DeliaExceptionHelper.throwError("sql-visitor-runaway", "oops");
        return "visitor-failed!";
    }

    private String genField(String mainAlias, String name) {
        if (mainAlias == null) {
            return name;
        } else {
            return String.format("%s.%s", mainAlias, name);
        }
    }

    private List<Tok.TokBase> runOneLoop(List<Tok.TokBase> currentList) {
        List<Tok.TokBase> newList = new ArrayList<>();
        int numToSkip = 0;
        int index = -1;
        for (Tok.TokBase exp : currentList) {
            index++;
            if (numToSkip > 0) {
                numToSkip--;
                continue;
            }

            if (exp instanceof Tok.PKWhereTok) {
                Tok.PKWhereTok pkto = (Tok.PKWhereTok) exp;
                StrCreator sc = new StrCreator();
                sc.o(pkto.strValue());
                StrFragment fragment = new StrFragment(sc.toString());
                newList.add(fragment);
            } else if (exp instanceof Tok.OperatorTok) {
                Tok.OperatorTok oexp = (Tok.OperatorTok) exp;
                if (oexp.op.equals("or") || oexp.op.equals("and")) {
                    StrCreator sc = new StrCreator();
                    if (nextTwoAreStrFragments(index, currentList, oexp.op, sc)) {
                        StrFragment fragment = new StrFragment(sc.toString());
                        if (oexp.negFlag) {
                            fragment.str = String.format("not (%s)", fragment.str);
                        }
                        newList.add(fragment);
                        numToSkip = 2;
                    } else {
                        newList.add(oexp);
                    }
                    continue; //skip for now
                }

                String opSql = opToSql(oexp.op);

                String op1 = WhereFunctionHelper.genNameOrFn(oexp.op1);
                String op2 = WhereFunctionHelper.genNameOrFn(oexp.op2);
                String fieldName1 = TokVisitorUtils.getPossibleFieldName(oexp.op1);
                String fieldName2 = TokVisitorUtils.getPossibleFieldName(oexp.op2);
                if (fieldName1 != null) {
                    op2 = "?";
                } else if (fieldName2 != null) {
                    op1 = "?";
                }

                StrCreator sc = new StrCreator();
                int extraToSkip = 0;
                if (oexp.op.equals("in")) {
                    int n = TokVisitorUtils.getDottedSize(oexp.op2);
                    extraToSkip = n - 1;
                    op2 = buildN("?", n);
                    sc.o("%s %s (%s)", op1, opSql, op2);
                } else {
                    sc.o("%s %s %s", op1, opSql, op2);
                }
                StrFragment fragment = new StrFragment(sc.toString());
                if (oexp.negFlag) {
                    fragment.str = String.format("not (%s)", fragment.str);
                }
                newList.add(fragment);
                numToSkip = 4 + extraToSkip;
            } else if (exp instanceof StrFragment) {
                newList.add(exp);
            }
        }
        return newList;
    }

    private String buildN(String s, int n) {
        if (n == 1) return "?";
        StringJoiner sj = new StringJoiner(",");
        for (int i = 0; i < n; i++) {
            sj.add("?");
        }
        return sj.toString();
    }

    private boolean nextTwoAreStrFragments(int index, List<Tok.TokBase> currentList, String op, StrCreator sc) {
        if (currentList.size() >= index + 2) {
            Tok.TokBase next1 = currentList.get(index + 1);
            Tok.TokBase next2 = currentList.get(index + 2);
            if (next1 instanceof StrFragment && next2 instanceof StrFragment) {
                StrFragment frag1 = (StrFragment) next1;
                StrFragment frag2 = (StrFragment) next2;

                String s1 = addParenIfNeeded(frag1.str);
                String s2 = addParenIfNeeded(frag2.str);

                sc.o("%s %s %s", s1, op, s2);
                return true;
            }
        }
        return false;
    }

    private String addParenIfNeeded(String str) {
        if (str.contains(" and ") || str.equals(" or ")) {
            return String.format("(%s)", str);
        }
        return str;
    }

    private String opToSql(String op) {
        return sqlValueRenderer.opToSql(op);
    }

}
