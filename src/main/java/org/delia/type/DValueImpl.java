package org.delia.type;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;


public class DValueImpl implements DValue, DValueInternal {
    private DType type;
    private Object object;
    private ValidationState valState = ValidationState.UNKNOWN;
    private Object persistenceId;

    public DValueImpl(DType type, Object object) {
        super();
        this.type = type;
        this.object = object;
    }

    @Override
    public DType getType() {
        return type;
    }
    @Override
    public Object getObject() {
        return object;
    }
    @Override
    public ValidationState getValidationState() {
        return valState;
    }
    @Override
    public boolean isValid() {
        return valState == ValidationState.VALID;
    }

    @Override    
    public void setValidationState(ValidationState valState) {
        this.valState = valState;
    }
    
    public void forceObject(Object obj) {
        this.object = obj;
    }
    public void forceType(DType type) {
        this.type = type;
    }

    @Override
    public int asInt() {
        if (object instanceof Integer) {
            Integer lval = (Integer) object;
            return lval.intValue();
        } else {
            Long lval = (Long) object;
            return lval.intValue();
        }
    }
    @Override
    public double asNumber() {
        if (object instanceof Integer) {
            Integer lval = (Integer) object;
            return lval.doubleValue();
        } else if (object instanceof Long){
            Long lval = (Long) object;
            return lval.doubleValue();
        } else {
        	Double lval = (Double) object;
        	return lval.doubleValue();
        }
    }
    @Override
    public long asLong() {
        if (object instanceof Integer) {
            Integer lval = (Integer) object;
            return lval.longValue();
        } else {
            Long lval = (Long) object;
            return lval.longValue();
        }
    }
    @Override
    public String asString() {
    	if (object instanceof WrappedDate) {
    		WrappedDate wdt = (WrappedDate) object;
    		return wdt.asString();
    	}
        return object.toString();
    }
    @Override
    public boolean asBoolean() {
        Boolean bool = (Boolean) object;
        return bool;
    }
    

	@Override
	public ZonedDateTime asDate() {
		WrappedDate wdt = (WrappedDate) object;
		return wdt.getDate();
	}
    @Override
    public Date asLegacyDate() {
		WrappedDate wdt = (WrappedDate) object;
        Date dt = wdt.getLegacyDate();
        return dt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String,DValue> asMap() {
        return (Map<String, DValue>) object;
    }
    
    @Override
    public DStructHelper asStruct() {
        return new DStructHelper(this);
    }

    @Override
    public Object getPersistenceId() {
        return persistenceId;
    }

    @Override
    public void setPersistenceId(Object persistenceId) {
        this.persistenceId = persistenceId;
    }

	@Override
	public DRelation asRelation() {
		DRelation drel = (DRelation) object;
		return drel;
	}
	
    //helps see typename in debugger
	@Override
	public String toString() {
		String s = "";
		if (object != null && type.isNumericShape()) {
			s = ": " + object.toString();
		}
		return type.toString() + s ;
	}
}