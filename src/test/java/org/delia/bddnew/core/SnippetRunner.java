package org.delia.bddnew.core;

public interface SnippetRunner {
    void setConnectionProvider(ConnectionProvider connProvider);
    BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes);
}
