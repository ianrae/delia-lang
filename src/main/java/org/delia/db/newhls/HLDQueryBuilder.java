package org.delia.db.newhls;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.newhls.cond.FilterCondBuilder;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

/**
 * Creates HLDQuery from a QueryExp
 * @author ian
 *
 */
public class HLDQueryBuilder {
	private DTypeRegistry registry;

	public HLDQueryBuilder(DTypeRegistry registry) {
		this.registry = registry;
	}

	public HLDQuery build(QueryExp queryExp) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = (DStructType) registry.getType(queryExp.typeName);
		hld.mainStructType = hld.fromType; //TODO fix
		hld.resultType = hld.fromType; //TODO fix

		FilterCondBuilder builder = new FilterCondBuilder();
		hld.filter = builder.build(queryExp);
		//			public List<StructField> throughChain = new ArrayList<>();
		//			public StructField finalField; //eg Customer.addr
		//			public List<FetchSpec> fetchL = new ArrayList<>(); //order matters: eg. .addr.fetch('country')
		//			public List<QueryFnSpec> funcL = new ArrayList<>(); //list and calc fns. order matters: eg. .addr.first().city

		return hld;
	}
}