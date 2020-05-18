package org.delia.db.hls.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaImpl;
import org.delia.api.DeliaSession;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.dval.DRelationHelper;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.util.DRuleHelper;
import org.delia.zdb.ZDBExecutor;

//normally we just call db directly. one 'let' statement = one call to db
public class DoubleHLSStragey implements HLSStragey {

	private DeliaSession session;
	private Log log;
	private Delia delia;

	public DoubleHLSStragey(Delia delia, DeliaSession session) {
		this.delia = delia;
		this.session = session; //may be null
		this.log = delia.getLog();
	}

	@Override
	public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, ZDBExecutor dbexecutor) {
		HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
		HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer
		QueryResponse qresp = dbexecutor.executeHLSQuery(hls, sql, qtx);

		//and again with span1
		//{Address->Address,MT:Address,[cust in [55,56],()}
		String field2 = determineRelField(hlspan1, hlspan2);//"cust"; //fix!!
		String deliaSrc = generateInQuery(hlspan1, hlspan2, qresp);
		LetStatementExp exp = compileInQuery(deliaSrc); 
		//src = "let x = Address[cust in [55,56]]

		HLSQueryStatement clone = new HLSQueryStatement();
		clone.hlspanL.add(hlspan1);
		clone.queryExp = (QueryExp) exp.value;
		clone.querySpec = new QuerySpec();
		clone.querySpec.queryExp = clone.queryExp;
		QueryResponse qresp2 = dbexecutor.executeHLSQuery(clone, sql, qtx);
		log.log("%b", qresp2.ok);

		//actually MEM already has address.cust as DRelation
		//we just need to fill in fetched items
		//merge results from qresp into qresp2 (add Customer to Address.cust)
		//			String field1 = hlspan1.rEl.rfieldPair.name; //.addr
		Map<DValue,List<DValue>> todoMap = new HashMap<>();
		for(DValue dval: qresp.dvalList) {  //for each customer
			//dval is a customer. find address in qresp2 whose .cust is pk of dval
			DValue inner = findIn(dval, qresp2, field2); //inner is address
			if (inner != null) {
				List<DValue> fklist = todoMap.get(inner);
				if (fklist == null) {
					fklist = new ArrayList<>();
				}
				fklist.add(dval);
				todoMap.put(inner, fklist); 
			}
		}

		for(DValue inner: todoMap.keySet()) {
			DValue innerInner = inner.asStruct().getField(field2); //cust
			DRelation drel = innerInner.asRelation();
			List<DValue> fklist = todoMap.get(inner);
			DRelationHelper.addToFetchedItems(drel, fklist);
		}

		return qresp2;
	}

	private DValue findIn(DValue dval, QueryResponse qresp2, String targetField) {
		DStructType structType = (DStructType) dval.getType();
		PrimaryKey pk = structType.getPrimaryKey(); 
		Object obj1 = dval.asStruct().getField(pk.getFieldName()).getObject();

		for(DValue x: qresp2.dvalList) {
			DValue inner = x.asStruct().getField(targetField);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				Object obj2 = drel.getForeignKey().getObject();
				if (obj1.equals(obj2)) {
					return x;
				}
			}
		}

		return null;
	}

	private LetStatementExp compileInQuery(String deliaSrc) {
		DeliaImpl deliaimpl = (DeliaImpl) delia;

		List<Exp> expL = deliaimpl.continueCompile(deliaSrc, session);
		LetStatementExp exp = findLetStatement(expL);
		return exp;
	}

	private String generateInQuery(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2, QueryResponse qresp) {
		//and again with span1
		HLSQueryStatement clone = new HLSQueryStatement();
		//{Address->Address,MT:Address,[cust in [55,56],()}

		String ss = hlspan1.fromType.getName();
		String field2 = determineRelField(hlspan1, hlspan2);//"cust"; //fix!!
		StringJoiner joiner = new StringJoiner(",");
		for(DValue dval: qresp.dvalList) {
			if (dval != null && dval.getType().isStructShape()) {
				DStructType structType = (DStructType) dval.getType();
				PrimaryKey pk = structType.getPrimaryKey(); 

				DValue pkvalue = dval.asStruct().getField(pk.getFieldName());
				joiner.add(pkvalue.asString());
			}
		}
		String deliaSrc = String.format("%s[%s in [%s]]", ss, field2, joiner.toString());
		log.log(deliaSrc);
		return deliaSrc;
	}

	private String determineRelField(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2) {
		String field1 = hlspan1.rEl.rfieldPair.name;
		RelationOneRule ruleOne = DRuleHelper.findOneRule(hlspan2.fromType, field1);
		if (ruleOne != null) {
			return ruleOne.relInfo.otherSide.fieldName; //can fail if one-sided
//			RelationInfo relinfo = DRuleHelper.findOtherSideOne(ruleOne.relInfo.farType, hlspan1.fromType);
//			return relinfo.fieldName;
		} else {
			RelationManyRule ruleMany = DRuleHelper.findManyRule(hlspan2.fromType, field1);
			return ruleMany.relInfo.otherSide.fieldName; //many always has otherSide
//			RelationInfo relinfo = DRuleHelper.findOtherSideMany(ruleMany.relInfo.farType, hlspan2.fromType);
//			return relinfo.fieldName;
		}
	}

	private LetStatementExp findLetStatement(List<Exp> expL) {
		for(Exp exp: expL) {
			if (exp instanceof LetStatementExp) {
				return (LetStatementExp) exp;
			}
		}
		return null;
	}

}