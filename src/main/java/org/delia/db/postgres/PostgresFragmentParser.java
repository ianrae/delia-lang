package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.LimitFragment;
import org.delia.db.sql.fragment.OrderByFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SelectStatementFragment;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
	public class PostgresFragmentParser extends SelectFragmentParser {
		
		public PostgresFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
			super(factorySvc, fpSvc);
		}
		
		protected void genFirst(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			selectFrag.clearFieldList();
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
			if (fieldName == null) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); 
				selectFrag.fieldL.add(fieldF);
			} else {
				FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
				selectFrag.fieldL.add(fieldF);
			}
			
			LimitFragment frag = new LimitFragment(1);
			selectFrag.limitFrag = frag;
		}
		protected void genLast(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
			
			selectFrag.clearFieldList();
			if (fieldName == null) {
				forceAddOrderByPrimaryKey(structType, selectFrag, "desc");
				TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
				if (pair == null) {
					DeliaExceptionHelper.throwError("last-requires-sortable-field", "last() requires an orderBy() function or a primary key in type '%s'", structType.getName());
				}
				fieldName = pair.name;
			} else {
				FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
				selectFrag.fieldL.add(fieldF);
			}
			
			String asc = "desc";
			OrderByFragment frag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
			addToOrderBy(frag, selectFrag);
			
			LimitFragment limitFrag = new LimitFragment(1);
			selectFrag.limitFrag = limitFrag;
		}
	}