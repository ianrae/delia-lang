package org.delia.zqueryresponse.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.zqueryresponse.ZQueryResponseFunctionBase;

public class ZDistinctFunction extends ZQueryResponseFunctionBase {

	public ZDistinctFunction(DTypeRegistry registry) {
		super(registry);
	}

	@Override
	public QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx) {
		List<DValue> dvalList = ctx.getDValList(); //use scope
		if (dvalList == null || dvalList.size() <= 1) {
			return qresp; //nothing to sort
		}
		
		List<DValue> newlist = new ArrayList<>();
		DValue firstVal = dvalList.get(0);
		if (firstVal.getType().isShape(Shape.RELATION)) {
			//build list of distinct fk values, using map
			List<DValue> distinctFKList = new ArrayList<>();
			Map<String,DValue> map = new HashMap<>();
			String typeName = null;
			for(DValue dval: dvalList) {
				DRelation drel = dval.asRelation();
				typeName = drel.getTypeName();
				for(DValue fk: drel.getMultipleKeys()) {
					String strval = fk.asString(); //use string for now
					if (! map.containsKey(strval)) {
						map.put(strval, fk);
						distinctFKList.add(fk);
					}
				}
			}
			
			DValue finalVal = this.createRelation(typeName, distinctFKList);
			newlist.add(finalVal);
		} else {
			//build list of distinct values, using map
			Map<String,DValue> map = new HashMap<>();
			for(DValue dval: dvalList) {
				String strval = dval.asString(); //use string for now
				if (! map.containsKey(strval)) {
					map.put(strval, dval);
					newlist.add(dval);
				}
			}
		}
		
		qresp.dvalList = newlist;
		return qresp;
	}
	
	
	private DValue createRelation(String typeName, List<DValue> fks) {
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildFromList(fks);
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
			return null;
		} else {
			DValue dval = builder.getDValue();
			return dval;
		}
	}
	
}