package org.delia.util;

import java.util.Map;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.dval.DValueExConverter;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;

public class PrimaryKeyHelperService extends ServiceBase {

	private DTypeRegistry registry;

	public PrimaryKeyHelperService(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	public boolean addPrimaryKeyIfMissing(QuerySpec spec, DValue partialVal) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(partialVal.getType());
		DValue inner = DValueHelper.findPrimaryKeyValue(partialVal);
		if (inner == null) {
			FilterEvaluator evaluator = spec.evaluator;

			DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
			String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
			inner = dvalConverter.buildFromObject(keyField, keyPair.type);
			
			Map<String,DValue> map = partialVal.asMap();
			map.put(keyPair.name, inner);
			return true;
		} else {
			return false;
		}
	}

	public void removePrimayKey(DValue dval) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		Map<String,DValue> map = dval.asMap();
		map.remove(keyPair.name);
	}
}
