package org.delia.db.sql.fragment;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
public class SelectFragmentParser extends FragmentParserBase {

	public SelectFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
		super(factorySvc, fpSvc);
	}

	public SelectStatementFragment parseSelect(QuerySpec spec, QueryDetails details) {
		SelectStatementFragment selectFrag = new SelectStatementFragment();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, selectFrag);
		selectFrag.tblFrag = tblFrag;

		initWhere(spec, structType, selectFrag);
		//			addJoins(spec, structType, selectFrag, details);
		addFns(spec, structType, selectFrag);

		generateQueryFns(spec, structType, selectFrag);

		fixupForParentFields(structType, selectFrag);
		if (needJoin(spec, structType, selectFrag, details)) {
			//used saved join if we have one
			if (savedJoinedFrag == null) {
				addJoins(spec, structType, selectFrag, details);
			} else {
				selectFrag.joinFrag = savedJoinedFrag;
			}
		}

		if (selectFrag.fieldL.isEmpty()) {
			FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
			selectFrag.fieldL.add(fieldF);
		}


		return selectFrag;
	}

	protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
		if (needJoinBase(spec, structType, selectFrag, details)) {
			return true;
		}

		if (selectFrag.joinFrag == null) {
			return false;
		}

		String alias = savedJoinedFrag.joinTblFrag.alias;

		boolean mentioned = false;
		if (selectFrag.orderByFrag != null) {
			if (alias.equals(selectFrag.orderByFrag.alias)) {
				mentioned = true;
			}
			for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
				if (alias.equals(obff.alias)) {
					mentioned = true;
					break;
				}
			}
		}


		if (mentioned) {
			log.log("need join..");
			return true;
		}
		return false;
	}


	public void generateQueryFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		this.doOrderByIfPresent(spec, structType, selectFrag);
		this.doLimitIfPresent(spec, structType, selectFrag);
		this.doOffsetIfPresent(spec, structType, selectFrag);
	}

	protected void doOrderByIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		QueryFuncExp qfexp = selectFnHelper.findFn(spec, "orderBy");
		if (qfexp == null) {
			return;
		}

		StringJoiner joiner = new StringJoiner(",");
		boolean isDesc = false;
		for(Exp exp : qfexp.argL) {
			if (exp instanceof IdentExp) {
				isDesc = exp.strValue().equals("desc");
			} else {
				String fieldName = exp.strValue();
				if (fieldName.contains(".")) {
					fieldName = StringUtils.substringAfter(fieldName, ".");
				}
				if (! DValueHelper.fieldExists(structType, fieldName)) {
					DeliaExceptionHelper.throwError("unknown-field", "type '%s' does not have field '%s'. Invalid orderBy parameter", structType.getName(), fieldName);
				}

				String alias = FragmentHelper.findAlias(structType, selectFrag);
				joiner.add(String.format("%s.%s", alias, fieldName));
			}
		}

		String asc = isDesc ? "desc" : null;
		OrderByFragment frag = FragmentHelper.buildRawOrderByFrag(structType, joiner.toString(), asc, selectFrag);
		addToOrderBy(frag, selectFrag);
	}
	protected void addToOrderBy(OrderByFragment frag, SelectStatementFragment selectFrag) {
		OrderByFragment orderByFrag = selectFrag.orderByFrag;
		if (orderByFrag == null) {
			selectFrag.orderByFrag = frag;
		} else {
			//only add if different
			if (areEqualOrderBy(selectFrag.orderByFrag, frag)) {
				return;
			}
			for(OrderByFragment obf: selectFrag.orderByFrag.additionalL) {
				if (areEqualOrderBy(obf, frag)) {
					return;
				}
			}

			OrderByFragment tmp = selectFrag.orderByFrag; //swap
			selectFrag.orderByFrag = frag;
			selectFrag.orderByFrag.additionalL.add(tmp);
		}
	}
	protected boolean areEqualOrderBy(OrderByFragment orderByFrag, OrderByFragment frag) {
		if((frag.alias != null &&frag.alias.equals(orderByFrag.alias)) && frag.name.equals(orderByFrag.name)) {
			if (frag.asc != null && frag.asc.equals(orderByFrag.asc)) {
				return true;
			} else if (frag.asc == null && orderByFrag.asc == null) {
				return true;
			}
		}
		return false;
	}

	protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
		if (qfexp == null) {
			return;
		}
		IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
		Integer n = exp.val;

		OffsetFragment frag = new OffsetFragment(n);
		selectFrag.offsetFrag = frag;
	}


	protected void addFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		//TODO: for now we implement exist using count(*). improve later
		if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
			genCount(spec, structType, selectFrag);
		} else if (selectFnHelper.isMinPresent(spec)) {
			genMin(spec, structType, selectFrag);
		} else if (selectFnHelper.isMaxPresent(spec)) {
			genMax(spec, structType, selectFrag);
		} else if (selectFnHelper.isFirstPresent(spec)) {
			genFirst(spec, structType, selectFrag);
		} else if (selectFnHelper.isLastPresent(spec)) {
			genLast(spec, structType, selectFrag);
		} else {
			//				sc.o("SELECT * FROM %s", typeName);
		}
	}

	protected void genCount(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "count");
		selectFrag.clearFieldList();
		if (fieldName == null) {
			FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
			fieldF.fnName = "COUNT";
			selectFrag.fieldL.add(fieldF);
		} else {
			addFnField("COUNT", fieldName, structType, selectFrag);
		}
	}
	protected void genMin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		selectFrag.clearFieldList();
		String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
		addFnField("MIN", fieldName, structType, selectFrag);
	}
	protected void genMax(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		selectFrag.clearFieldList();
		String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
		addFnField("MAX", fieldName, structType, selectFrag);
	}
	protected void genFirst(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
		AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
		selectFrag.earlyL.add(top);
		selectFrag.clearFieldList();
		if (fieldName == null) {
			FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); 
			selectFrag.fieldL.add(fieldF);
		} else {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			selectFrag.fieldL.add(fieldF);
		}
	}
	protected void genLast(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
		String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
		AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
		selectFrag.earlyL.add(top);
		selectFrag.clearFieldList();
		if (fieldName == null) {
			forceAddOrderByPrimaryKey(structType, selectFrag, "desc");
		} else {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			selectFrag.fieldL.add(fieldF);
			forceAddOrderByField(structType, fieldName, "desc", selectFrag);
		}
	}


	protected void forceAddOrderByPrimaryKey(DStructType structType, SelectStatementFragment selectFrag, String asc) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
		if (pair == null) {
			return; //no primary key
		}
		forceAddOrderByField(structType, pair.name, asc, selectFrag);
	}
	protected void forceAddOrderByField(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
		OrderByFragment orderByFrag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
		selectFrag.orderByFrag = orderByFrag;
	}

	protected void addFnField(String fnName, String fieldName, DStructType structType, SelectStatementFragment selectFrag) {
		FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
		if (fieldF == null) {
			DeliaExceptionHelper.throwError("unknown-field", "Field %s.%s unknown in %s function", structType.getName(), fieldName, fnName);
		}
		fieldF.fnName = fnName;
		addOrReplace(selectFrag, fieldF);
	}

	public String renderSelect(SelectStatementFragment selectFrag) {
		selectFrag.statement.sql = selectFrag.render();
		return selectFrag.statement.sql;
	}
}