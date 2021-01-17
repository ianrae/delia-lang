package org.delia.db.memdb.filter;

import java.time.ZonedDateTime;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.dval.DValueConverterService;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class RelationOpEvaluator extends OpEvaluatorBase {

	private DValueConverterService converterSvc;
	
	public RelationOpEvaluator(OP op, String fieldName, FactoryService factorySvc) {
		super(op, fieldName);
		this.converterSvc = new DValueConverterService(factorySvc);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
	      Boolean b = checkNull(dval, rightVar);
	      if (b != null) {
	        return b;
	      }
		DRelation drel1 = dval.asStruct().getField(fieldName).asRelation();
		if (drel1.isMultipleKey()) {
			for(DValue kk: drel1.getMultipleKeys()) {
				if (doInnerMatch(kk)) {
					return true;
				}
			}
			return false;
		}
		
		//we are checking if dval.fieldName (a relation) has an FK equal to rightVal
		//However, the relation may be empty (if relation is optinal).
		if (drel1.getMultipleKeys().size() == 0) {
			return false;
		}
		DValue keyVal = drel1.getForeignKey();
		return doInnerMatch(keyVal);
	}

	private boolean doInnerMatch(DValue keyVal) {
		Shape shape = keyVal.getType().getShape();
		switch(shape) {
		case INTEGER:
			return doInnerMatchInt(keyVal);
		case LONG:
			return doInnerMatchLong(keyVal);
		case NUMBER:
			return doInnerMatchNumber(keyVal);
		case STRING:
			return doInnerMatchString(keyVal);
//		case BOOLEAN: TODO do we support bookean fk?
//			return doInnerMatchBoolean(keyVal);
		case DATE:
			return doInnerMatchDate(keyVal);
		default:
			DeliaExceptionHelper.throwError("unsupported-fk-type", "Shape %s not supported", shape.name());
		}
		return false;
	}
	
	private Number getRightValAsNumber() {
		Object obj = converterSvc.extractObj((Exp)rightVar);
		return (Number) obj;
	}

	private boolean doInnerMatchInt(DValue keyVal) {
		Integer n1 = keyVal.asInt(); 
		Integer n2 = getRightValAsNumber().intValue();

		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}
	private boolean doInnerMatchLong(DValue keyVal) {
		Long n1 = keyVal.asLong();
		Long n2 = getRightValAsNumber().longValue();

		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}	
	private boolean doInnerMatchNumber(DValue keyVal) {
		Double n1 = keyVal.asNumber();
		Double n2 = getRightValAsNumber().doubleValue();

		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}	
	private boolean doInnerMatchString(DValue keyVal) {
		String n1 = keyVal.asString();
		String n2 = ((Exp)rightVar).strValue();

		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}	
	private boolean doInnerMatchDate(DValue keyVal) {
		ZonedDateTime n1 = keyVal.asDate();
		ZonedDateTime n2 = null; //TOD fix this ((StringExp)rightVar).val; //TODO: can this sometimes be IntegerExp

		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}	
	
}