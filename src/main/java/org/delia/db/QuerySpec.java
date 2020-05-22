package org.delia.db;

import org.delia.compiler.ast.QueryExp;
import org.delia.runner.FilterEvaluator;

/**
 * Parsed query, such as Customer[id != 100].orderBy('createDate')
 * 
 * @author Ian Rae
 *
 */
public class QuerySpec {
	public QueryExp queryExp;
	public FilterEvaluator evaluator;
}