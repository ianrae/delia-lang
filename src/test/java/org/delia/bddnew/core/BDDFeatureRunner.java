package org.delia.bddnew.core;

import org.delia.error.DeliaError;
import org.delia.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BDDFeatureRunner {
    private final Log log;
    private Map<SnippetType, SnippetRunner> runnerMap = new HashMap<>();
    private BDDSnippetResult mostRecentRes;
    private ConnectionProvider connectionProvider;
    private int singleTestToRunIndex = -1;

    public BDDFeatureRunner(ConnectionProvider connProvider, Log log) {
        this.connectionProvider = connProvider;
        this.log = log;
    }

    public void addRunner(SnippetType type, SnippetRunner runner) {
        runnerMap.put(type, runner);
        runner.setConnectionProvider(connectionProvider);
    }

    public BDDFeatureResult runTests(BDDFeature feature, String fileName) {
        BDDFeatureResult res = new BDDFeatureResult();
        log.log("=============== FEATURE: %s (%s) ======================", feature.name, fileName);
        if (!executeBackground(feature.backgroundsL)) {
            return res;
        }

        int index = 0;
        for (BDDTest test : feature.testsL) {
            if (singleTestToRunIndex >= 0) {
                if (index == singleTestToRunIndex) {
                    boolean b = executeTest(test, index);
                    if (b) {
                        res.numPass++;
                    } else {
                        res.numFail++;
                    }
                    break;
                }
            } else {
                if (test.skip) {
                    log.log("SKIP: %s", test.title);
                    res.numSkip++;
                } else {
                    boolean b = executeTest(test, index);
                    if (b) {
                        res.numPass++;
                    } else {
                        res.numFail++;
                    }
                }
            }
            index++;
        }

        String strFail = res.numFail == 0 ? "FAIL" : "**FAIL**";
        int total = res.numPass + res.numFail + res.numSkip;
        log.log("PASS:%d, %s:%d, SKIPPED:%d tests (%d)", res.numPass, strFail, res.numFail, res.numSkip, total);
        return res;
    }

    private boolean executeBackground(List<BDDSnippet> backgroundsL) {
        for (BDDSnippet snippet : backgroundsL) {
            BDDSnippetResult tres = executeSnippet(snippet, mostRecentRes);
            if (!tres.ok) {
                return false;
            }
        }
        return true;
    }

    private BDDSnippetResult executeSnippet(BDDSnippet snippet, BDDSnippetResult previousRes) {
        snippet.dbType = connectionProvider.getDBType();
        SnippetRunner runner = runnerMap.get(snippet.type);
        BDDSnippetResult res = runner.execute(snippet, previousRes);

        Map<String, String> hintMap = previousRes == null ? null : previousRes.nameHintMap;
        mostRecentRes = res;
        if (mostRecentRes.nameHintMap == null || mostRecentRes.nameHintMap.isEmpty()) {
            mostRecentRes.nameHintMap = hintMap;
        }
        return res;
    }

    private boolean executeTest(BDDTest test, int index) {
        log.log("");
        log.log("---------------------------------------");
        log.log(String.format("Test%d: %s...", index, test.title));
        BDDSnippetResult failingTRes = null;
        for (BDDSnippet snippet : test.givenL) {
            BDDSnippetResult tres = executeSnippet(snippet, mostRecentRes);
            if (!tres.ok) {
                failingTRes = tres;
            }
        }

        BDDSnippetResult whenRes = null;
        if (failingTRes == null) {
            for (BDDSnippet snippet : test.whenL) {
                BDDSnippetResult tres = executeSnippet(snippet, mostRecentRes);
                whenRes = tres;
                //the when part may have the expected error so keep going
                if (!tres.ok) {
                    failingTRes = tres;
                }
            }
        }

        if (whenRes == null && failingTRes != null) {
            whenRes = new BDDSnippetResult();
            whenRes.ok = false;
            whenRes.errors = new ArrayList<>(failingTRes.errors);
        }

        for (BDDSnippet snippet : test.thenL) {
            BDDSnippetResult tres = executeSnippet(snippet, whenRes);
            if (!tres.ok) {
                if (failingTRes == null) {
                    failingTRes = tres;
                } else {
                    for (DeliaError err : tres.errors) {
                        if (failingTRes != null && !failingTRes.errors.contains(err)) {
                            failingTRes.errors.add(err);
                        }
                    }
                }
            } else {
                failingTRes = null; //reset
            }
        }

        if (failingTRes != null && !failingTRes.ok) {
            log.log("**Test%d: %s FAILED!** (in given)", index, test.title);
            return false;
        }

        return true;
    }

    public int getSingleTestToRunIndex() {
        return singleTestToRunIndex;
    }

    public void setSingleTestToRunIndex(int singleTestToRunIndex) {
        this.singleTestToRunIndex = singleTestToRunIndex;
    }

}
