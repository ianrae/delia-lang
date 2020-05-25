package org.delia.db.memdb.filter;

import java.util.Date;

import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class RelationOpEvaluator extends OpEvaluatorBase {

	public RelationOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
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
		Integer n1 = keyVal.asInt(); //TODO: string later
		Integer n2 = ((IntegerExp)rightVar).val;

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

	private boolean doInnerMatchInt(DValue keyVal) {
		Integer n1 = keyVal.asInt(); 
		Integer n2 = ((IntegerExp)rightVar).val;

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
		Long n2 = ((LongExp)rightVar).val; //TODO: can this sometimes be IntegerExp

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
		Double n2 = ((NumberExp)rightVar).val; //TODO: can this sometimes be IntegerExp

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
		String n2 = ((StringExp)rightVar).val; //TODO: can this sometimes be IntegerExp

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
		Date n1 = keyVal.asLegacyDate();
		Date n2 = null; //TOD fix this ((StringExp)rightVar).val; //TODO: can this sometimes be IntegerExp

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