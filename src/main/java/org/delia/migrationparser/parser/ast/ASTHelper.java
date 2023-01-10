package org.delia.migrationparser.parser.ast;

import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class ASTHelper {

    public static DStructType findType(DTypeRegistry registry, String typeName) {
        DStructType structType = registry.getStructType(new DTypeName(null, typeName)); //TODO support schema later
        if (structType == null) {
            DeliaExceptionHelper.throwError("type.not.found.in.migration", "Migration: can't find type '%s'", typeName);
        }
        return structType;
    }
}
