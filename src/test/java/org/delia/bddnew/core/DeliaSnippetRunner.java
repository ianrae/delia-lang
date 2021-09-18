package org.delia.bddnew.core;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.base.DBHelper;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.runner.ResultValue;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StringUtil;

public class DeliaSnippetRunner implements SnippetRunner {
    private final Log log;
    private final Log deliaLog;
    private DeliaSession sess;
    private ConnectionProvider connProvider;

    public DeliaSnippetRunner(Log log, Log deliaLog) {
        this.log = log;
        this.deliaLog = deliaLog;
    }

    @Override
    public void setConnectionProvider(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }

    @Override
    public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
        BDDSnippetResult res = new BDDSnippetResult();

        DeliaGenericDao dao;
        if (previousRes != null && previousRes.sess != null) {
            dao = new DeliaGenericDao(previousRes.sess.getDelia(), previousRes.sess);
            sess = previousRes.sess;
        } else {
            ConnectionDefinition connDef = connProvider.getConnectionDef();
            dao = new DeliaGenericDao(connDef, deliaLog);
        }

        String src = StringUtil.flattenEx(snippet.lines, "\n");
        log.log("deliaSrc: %s", src);
        if (!src.isEmpty()) {
            if (sess == null) {
                if (!dao.initialize(src)) {
                    res.ok = false;
                    //add errs
                    return res;
                }
                sess = dao.getMostRecentSession();
                res.sess = sess;
            } else {
                ResultValue resValue = dao.getDelia().continueExecution(src, sess);
                res.resValue = resValue;
                res.sess = sess;
                if (!resValue.ok) {
                    res.ok = false;
                    //add errs
                    return res;
                }
            }
        }

        Delia delia = sess.getDelia();
        if (DBType.MEM.equals(delia.getDBInterface().getDBType())) {
            for (String typeName : sess.getExecutionContext().registry.getAll()) {
                DBHelper.createTable(delia.getDBInterface(), typeName);
            }
        }

        //build name hints. The parent side of a DValue relation doesn't exist in the db
        //so we need to propogate the field name so the tests can produce identical parent relation field
        for (String typeName : sess.getExecutionContext().registry.getAll()) {
            DType type = sess.getExecutionContext().registry.getType(typeName);
            if (type.isStructShape()) {
                DStructType structType = (DStructType) type;
                for(TypePair pair: structType.getAllFields()) {
                    if (DRuleHelper.isParentRelation(structType, pair)) {
                        RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
                        String key = String.format("%s.%s", relinfo.otherSide.nearType.getName(), relinfo.otherSide.fieldName);
                        res.nameHintMap.put(key, relinfo.fieldName);
                    }
                }
            }
        }

        res.ok = true;
        return res;
    }
}
