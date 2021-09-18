//package org.delia.bddnew.core;
//
//import org.delia.db.sql.ConnectionDefinition;
//import org.delia.log.Log;
//import org.delia.util.StringUtil;
//
//import java.util.stream.Collectors;
//
//public class SeedeSnippetRunner implements SnippetRunner {
//    private final Log log; //bdd log
//    private final Log seedeLog; //log used by SdExecutor
//    private final Log deliaLog;
//    private ConnectionProvider connProvider;
//
//    public SeedeSnippetRunner(Log log, Log seedeLog, Log deliaLog) {
//        this.log = log;
//        this.seedeLog = seedeLog;
//        this.deliaLog = deliaLog;
//    }
//
//    @Override
//    public void setConnectionProvider(ConnectionProvider connProvider) {
//        this.connProvider = connProvider;
//    }
//    @Override
//    public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
//        BDDSnippetResult res = new BDDSnippetResult();
//
//        DBSchemaBuilder schemaBuilder = getSchemaBuilder(previousRes);
//        try (SdExecutor executor = createExecutor(schemaBuilder, previousRes)) {
//            executor.getOptions().csvPath = "./src/test/resources/test/northwind/"; //hard-code for now
//            String json = StringUtil.flattenEx(snippet.lines, "\n");
//            SdExecutorResults sdres = executor.runScript(json);
//            if (!sdres.ok) {
//                sdres.errors.forEach(err -> log.log(err.toString()));
//                res.errors = sdres.errors.stream().map(x -> new SdError(x.getId(), x.getMsg())).collect(Collectors.toList());
//                return res;
//            } else {
//                previousRes.sess = sdres.sess;
//            }
//        }
//
//        res.ok = true;
//        res.sess = previousRes.sess;
//        return res;
//    }
//
//    private SdExecutor createExecutor(DBSchemaBuilder schemaBuilder, BDDSnippetResult previousRes) {
//        if (previousRes.sess != null) {
//            return new SdExecutor(schemaBuilder, previousRes.sess, seedeLog, previousRes.nameHintMap);
//        } else {
//            ConnectionDefinition connDef = connProvider.getConnectionDef();
//            return new SdExecutor(schemaBuilder, connDef, seedeLog, deliaLog, previousRes.nameHintMap);
//        }
//    }
//
//    private DBSchemaBuilder getSchemaBuilder(BDDSnippetResult previousRes) {
////        MEMSchemaBuilder builder = new MEMSchemaBuilder(previousRes.sess);
//        return connProvider.getSchemaBuilder(previousRes.sess, seedeLog);
//    }
//}
