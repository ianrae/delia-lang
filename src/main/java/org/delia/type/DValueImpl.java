package org.delia.type;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import org.delia.log.LoggableBlob;
import org.delia.util.BlobUtils;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;


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
    	} else if (object instanceof WrappedBlob) {
    		WrappedBlob wblob = (WrappedBlob) object;
    		return BlobUtils.toBase64(wblob.getByteArray()); //TODO: fails if file. fix later
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
    public WrappedBlob asBlob() {
		WrappedBlob wblob = (WrappedBlob) object;
		return wblob;
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
		if (object != null && type.isStructShape()) {
            PrimaryKey primaryKey = DValueHelper.findPrimaryKeyField(getType());
            String str = "";
            if (primaryKey.isMultiple()) {
                ListWalker<TypePair> walker1 = new ListWalker<>(primaryKey.getKeys());
                StrCreator sc = new StrCreator();
                sc.addStr("{");
                while (walker1.hasNext()) {
                    TypePair pair = walker1.next();
                    sc.o("%s", pair.name);
                    walker1.addIfNotLast(sc, ",");
                }
                sc.addStr("}");
                str = sc.toString();
            } else {
                DValue pkval = DValueHelper.findPrimaryKeyValue(this);
                if (pkval != null) {
                    str = pkval.toString();
                }
            }

			s = "." + str;
		} else if (object != null && type.isNumericShape()) {
			s = ": " + object.toString();
		} else if (object != null && type.isShape(Shape.BLOB)) {
		    if (object instanceof WrappedBlob) {
                WrappedBlob wblob = (WrappedBlob) object;
                LoggableBlob lb = new LoggableBlob(wblob.getByteArray());
                s = ": " + lb.toString();
            }
		}
		return type.toString() + s ;
	}
}