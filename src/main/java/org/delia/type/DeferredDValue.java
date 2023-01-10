package org.delia.type;

import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.util.DeliaExceptionHelper;

/**
 * A placeholder DValue, which initially contains a fake value.
 * It is used because during HLD,LLD,SQl generation we don't
 * know the value of delia vars, since they haven't been executed yet.
 * <p>
 * At runtime, we detect these 'deferred' values and resolve them
 * into their real value, which is used in the sql.
 * <p>
 * Example
 * let x = 5
 * insert Customer { 1, x}
 */
public class DeferredDValue extends DValueImpl {
    public String deliaVarName;

    public DeferredDValue(DType type, Object object, String deliaVarName) {
        super(type, object);
        this.deliaVarName = deliaVarName;
    }

    public void resolveTo(DValue finalValue) {
        if (finalValue == null) {
            forceObject(null);
        } else {
            if (finalValue.getType().getShape() != getType().getShape()) {
                DeliaExceptionHelper.throwError("deferred-dvalue-resolve-wrong-type", "Can't resolve '%s' to '%s'", getType().getName(), finalValue.getType().getName());
            }
            forceObject(finalValue.getObject());
        }
    }
}
