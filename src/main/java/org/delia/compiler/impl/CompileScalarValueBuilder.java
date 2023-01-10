package org.delia.compiler.impl;

import org.delia.core.FactoryService;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.DValueBuilder;
import org.delia.valuebuilder.IntegerValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;

/**
 * Different error message than normal ScalarValueBuilder
 */
public class CompileScalarValueBuilder extends ScalarValueBuilder {

    public CompileScalarValueBuilder(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc, registry);
    }

    //handle the largest int we support (long)
    public DValue buildEffectiveLongInt(String input) {
        DType dtype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
        IntegerValueBuilder builder = new IntegerValueBuilder(dtype);
        builder.buildFromStringEx(input, EffectiveShape.EFFECTIVE_LONG);
        return finish(builder, "int", input);
    }

    @Override
    protected void throwOnFail(DValueBuilder builder, String typeStr, Object value) {
        //FUTURE propogate errors from inner builder
        String s = value == null ? "NULL" : value.toString();
        String msg = String.format("%s value is not an %s - %s", typeStr, typeStr, s);
        et.add("wrong-type", msg);
        DeliaExceptionHelper.throwError("wrong-type", msg);
    }
}
