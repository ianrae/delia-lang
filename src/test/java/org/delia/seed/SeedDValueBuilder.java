package org.delia.seed;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.codegen.DeliaEntity;
import org.delia.codegen.DeliaImmutable;
import org.delia.core.ServiceBase;
import org.delia.dval.DRelationHelper;
import org.delia.dval.DValueConverterService;
import org.delia.error.DetailedError;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.PartialStructValueBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class SeedDValueBuilder extends ServiceBase {
    private static class InsertExtraInfo {
        public DValue generatedSerialValue;
    }

    private DeliaSession mainSession; //not used directly. we create child sessions
    protected Delia delia;
    protected DValueConverterService dvalConverter;
    protected DTypeRegistry registry;
    protected ScalarValueBuilder scalarBuilder;
    protected String typeName;
    protected DStructType structType;

    public SeedDValueBuilder(DeliaSession session, String typeName) {
        super(session.getDelia().getFactoryService());
        this.typeName = typeName;
        this.mainSession = session;
        this.delia = session.getDelia();
        this.registry = session.getExecutionContext().registry;
        this.structType = (DStructType) registry.getType(typeName);

        this.dvalConverter = new DValueConverterService(factorySvc);
        this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
    }

    public List<DValue> xxdoInsertOrUpdate(boolean isInsert, DeliaEntity entity, String src, InsertExtraInfo extraInfo) {
        List<DValue> inputL = new ArrayList<>();
        if (entity != null) {
            if (isInsert) {
                inputL.add(createDValue(entity));
            } else {
                inputL.add(createPartialDValue(entity));
            }
        }
        return inputL;
    }

    private DValue createPartialDValue(DeliaEntity obj) {
        if (obj instanceof DeliaEntity) {
            DeliaEntity entity = (DeliaEntity) obj;
            PartialStructValueBuilder builder = new PartialStructValueBuilder(structType);
            return this.buildFromEntity(entity, typeName, builder);
        } else {
            return null; // return obj.internalDValue();
        }
    }

    protected DValue createDValue(DeliaEntity obj) {
        if (obj instanceof DeliaEntity) {
            DeliaEntity entity = (DeliaEntity) obj;
            StructValueBuilder builder = new StructValueBuilder(structType);
            return this.buildFromEntity(entity, typeName, builder);
        } else {
            return null; //return obj.internalDValue();
        }
    }

    protected DValue buildFromEntity(DeliaEntity entity, String typeName, StructValueBuilder builder) {
        if (entity.internalSetValueMap().isEmpty()) {
            DeliaImmutable immut = (DeliaImmutable) entity;
            return immut.internalDValue();
        }

        for (TypePair pair : structType.getAllFields()) {
            String fieldName = pair.name;
            if (entity.internalSetValueMap().containsKey(fieldName)) {
                Object val = entity.internalSetValueMap().get(fieldName);
                if (val instanceof DeliaImmutable) {
                    DeliaImmutable immut = (DeliaImmutable) val;
                    DValue dval = immut.internalDValue();
                    if (DRelationHelper.isRelation(structType, fieldName)) {
                        if (dval != null) {
                            dval = createEmptyRelation(structType, fieldName, dval);
                        } else if (immut instanceof DeliaEntity) {
                            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(pair.type);
                            if (pkpair != null) {
                                DeliaEntity innerEntity = (DeliaEntity) immut;
                                Object pkvalue = innerEntity.internalSetValueMap().get(pkpair.name);
                                DValue pkVal = dvalConverter.buildFromObject(pkvalue, pkpair.type.getShape(), scalarBuilder);
                                dval = createEmptyRelationEx(structType, fieldName, pkVal);
                            }
                        }
                    }
                    builder.addField(fieldName, dval);
                } else {
                    DValue dval = dvalConverter.buildFromObject(val, pair.type.getShape(), scalarBuilder);
                    builder.addField(fieldName, dval);
                }
            } else {
                DeliaImmutable immut = (DeliaImmutable) entity;
                DValue internalDval = immut.internalDValue(); //can be null if disconnected
                if (internalDval == null) {
                    if (!structType.fieldIsSerial(fieldName)) {
                        builder.addField(fieldName, null);
                    }
                } else {
                    DValue dval = immut.internalDValue().asMap().get(fieldName); //may get null
                    builder.addField(fieldName, dval);
                }
            }
        }

        boolean b = builder.finish();
        if (!b) {
            DetailedError err = builder.getValidationErrors().get(builder.getValidationErrors().size() - 1);
            String msg = String.format("Type %s: field %s: %s", err.getTypeName(), err.getFieldName(), err.toString());
            DeliaExceptionHelper.throwError("dao-error", msg);
        }
        DValue finalVal = builder.getDValue();
        return finalVal;
    }

    private DValue createEmptyRelation(DStructType structType, String fieldName, DValue relValue) {
        DValue newVal = DRelationHelper.createEmptyRelation(structType, fieldName, registry);
        if (newVal != null) {
            DRelationHelper.addFK(newVal, relValue);
        }
        return newVal;
    }

    private DValue createEmptyRelationEx(DStructType structType, String fieldName, DValue fkval) {
        DValue newVal = DRelationHelper.createEmptyRelation(structType, fieldName, registry);
        if (newVal != null) {
            DRelation drel = newVal.asRelation();
            drel.addKey(fkval);
        }
        return newVal;
    }

    protected DValue getPrimaryKeyValue(DeliaImmutable obj) {
        PrimaryKey pk = structType.getPrimaryKey();
        if (obj instanceof DeliaEntity) {
            String fieldName = pk.getKey().name;
            DeliaEntity entity = (DeliaEntity) obj;
            if (entity.internalSetValueMap().containsKey(fieldName)) {
                Object val = entity.internalSetValueMap().get(fieldName);
                DValue dval = dvalConverter.buildFromObject(val, pk.getKey().type.getShape(), scalarBuilder);
                return dval;
            }
        }

        DValue pkval = obj.internalDValue().asStruct().getField(pk.getFieldName());
        return pkval;

    }

}