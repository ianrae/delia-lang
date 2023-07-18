package org.delia.bddnew.core;

import org.delia.base.DbTableCleaner;
import org.delia.error.DeliaError;
import org.delia.log.DeliaLog;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BDDFeatureRunner {
    private final DeliaLog log;
    private Map<SnippetType, SnippetRunner> runnerMap = new HashMap<>();
    private BDDSnippetResult mostRecentRes;
    private ConnectionProvider connectionProvider;
    private boolean runBackgroundPerTest; //false means run background once per feature
    private int singleTestToRunIndex = -1;

    public BDDFeatureRunner(ConnectionProvider connProvider, DeliaLog log) {
        this.connectionProvider = connProvider;
        this.log = log;
    }

    public void addRunner(SnippetType type, SnippetRunner runner) {
        runnerMap.put(type, runner);
        runner.setConnectionProvider(connectionProvider);
    }

    public BDDFeatureResult runTests(BDDFeature feature, String fileName) {
        runBackgroundPerTest = feature.runBackgroundPerTest;
        BDDFeatureResult res = new BDDFeatureResult();
        log.log("=============== FEATURE: %s (%s) ======================", feature.name, fileName);
        boolean haveCleandedDB = false;
        if (!runBackgroundPerTest) {
            DbTableCleaner cleaner = new DbTableCleaner();
            cleaner.cleanDB(connectionProvider.getDBType());
            haveCleandedDB = true;
            if (!executeBackground(feature.backgroundsL)) {
                return res;
            }
        }

        int index = 0;
        for (BDDTest test : feature.testsL) {
            if (singleTestToRunIndex >= 0) {
                if (index == singleTestToRunIndex) {
                    boolean b = executeTest(test, index, feature, haveCleandedDB);
                    if (b) {
                        res.numPass++;
                    } else {
                        res.numFail++;
                    }
                    break;
                }
            } else {
                if (test.skip) {
                    log.log("SKIP: %s", getTestTitle(test));
                    res.numSkip++;
                } else {
                    boolean b = executeTest(test, index, feature, haveCleandedDB);
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
        log.log("");
        log.log("*** PASS:%d, %s:%d, SKIPPED:%d tests (%d) ***", res.numPass, strFail, res.numFail, res.numSkip, total);
        return res;
    }

    private boolean executeBackground(List<BDDSnippet> backgroundsL) {
        BDDSnippetResult lastRes = null;
        for (BDDSnippet snippet : backgroundsL) {
            BDDSnippetResult tres = executeSnippet(snippet, mostRecentRes);
            lastRes = tres;
            if (!tres.ok) {
                return false;
            }
        }
        //TODO: fix later. this is hacky
        if (lastRes != null) {
            if (lastRes.sess != null) {
                if (lastRes.sess.getExecutionContext().registry.getAll().size() == DTypeRegistry.NUM_BUILTIN_TYPES) {
                    mostRecentRes = null;
                }
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

    private boolean executeTest(BDDTest test, int index, BDDFeature feature, boolean haveCleandedDB) {
        log.log("");
        log.log("-------------------------------------------------------");
        log.log(String.format("Test%d: %s...", index, getTestTitle(test)));
        if (!haveCleandedDB && (index == 0 || singleTestToRunIndex == index || !test.chainNextTest)) {
//            log.log("clearrecentRes");
            if (! test.chainNextTest) {
                mostRecentRes = null;
            }
            DbTableCleaner cleaner = new DbTableCleaner();
            cleaner.cleanDB(connectionProvider.getDBType());
        }

        if (runBackgroundPerTest) {
            if (!executeBackground(feature.backgroundsL)) {
                return false;
            }
        }

        //given
        BDDSnippetResult failingTRes = null;
        for (BDDSnippet snippet : test.givenL) {
            snippet.thenType = feature.expectedType;
            snippet.bulkInsertEnabled = test.bulkInsertEnabled;
            BDDSnippetResult tres = executeSnippet(snippet, mostRecentRes);
            if (!tres.ok) {
                failingTRes = tres;
            }
        }

        //when
        BDDSnippetResult whenRes = null;
        if (failingTRes == null) {
            for (BDDSnippet snippet : test.whenL) {
                snippet.thenType = feature.expectedType;
                snippet.bulkInsertEnabled = test.bulkInsertEnabled;
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

        //propogate if needed
        if (whenRes != null && whenRes.resValue == null && !whenRes.errors.isEmpty()) {
            whenRes.resValue = new ResultValue();
            whenRes.resValue.ok = false;
            whenRes.resValue.errors.addAll(whenRes.errors);
        }

        //then
        for (BDDSnippet snippet : test.thenL) {
            snippet.thenType = feature.expectedType;
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
            log.log("**Test%d: %s FAILED!** (in given)", index, getTestTitle(test));
            return false;
        }

        return true;
    }

    private String getTestTitle(BDDTest test) {
        String title = test.title == null ? "" : test.title;
        return title;
    }

    public int getSingleTestToRunIndex() {
        return singleTestToRunIndex;
    }

    public void setSingleTestToRunIndex(int singleTestToRunIndex) {
        this.singleTestToRunIndex = singleTestToRunIndex;
    }

}
