package org.delia.dao;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.codegen.DeliaEntity;
import org.delia.codegen.DeliaImmutable;
import org.delia.core.ServiceBase;
import org.delia.dval.DRelationHelper;
import org.delia.dval.DValueConverterService;
import org.delia.error.DetailedError;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;

public abstract class EntityDaoBase<T extends DeliaImmutable> extends ServiceBase {
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

	public EntityDaoBase(DeliaSession session, String typeName) {
		super(session.getDelia().getFactoryService());
		this.typeName = typeName;
		this.mainSession = session;
		this.delia = session.getDelia();
		this.registry = session.getExecutionContext().registry;
    	this.structType = (DStructType) registry.getType(typeName);

		this.dvalConverter = new DValueConverterService(factorySvc);
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}
	
	protected ResultValue doInsertOrUpdate(T entity, String src, InsertExtraInfo extraInfo) {
		List<DValue> inputL = new ArrayList<>();
		if (entity != null) {
			inputL.add(createDValue(entity));
		}
		DValueIterator iter = new DValueIterator(inputL);	
		DaoRunnerInitializer dri = new DaoRunnerInitializer(iter);
		
		//use child session for thread-safety and isolation
		DeliaSession session = mainSession.createChildSession();
		session.setRunnerIntiliazer(dri);
		ResultValue res = delia.continueExecution(src, session);
		session.setRunnerIntiliazer(null);
		if (extraInfo != null) {
			ResultValue res2 = session.getExecutionContext().varMap.get(RunnerImpl.VAR_SERIAL);
			if (res2 != null) {
				extraInfo.generatedSerialValue = res2.ok ? res2.getAsDValue() : null;
			}
		}
		return res;
	}
	
    protected DValue createDValue(T obj) {
    	if (obj instanceof DeliaEntity) {
    		DeliaEntity entity = (DeliaEntity) obj;
    		return this.buildFromEntity(entity, typeName);
    	} else {
    		return obj.internalDValue();
    	}
    }

    protected DValue buildFromEntity(DeliaEntity entity, String typeName) {
    	if (entity.internalSetValueMap().isEmpty()) {
    		DeliaImmutable immut = (DeliaImmutable) entity;
    		return immut.internalDValue();
    	}
    		
    	StructValueBuilder builder = new StructValueBuilder(structType);
    	for(TypePair pair: structType.getAllFields()) {
    		String fieldName = pair.name;
    		if (entity.internalSetValueMap().containsKey(fieldName)) {
    			Object val = entity.internalSetValueMap().get(fieldName);
    			if (val instanceof DeliaImmutable) {
    				DeliaImmutable immut = (DeliaImmutable) val;
    				DValue dval = immut.internalDValue();
    				if (DRelationHelper.isRelation(structType, fieldName) && dval != null) {
    					dval = createEmptyRelation(structType, fieldName, dval);
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
    		DeliaExceptionHelper.throwError("badsomething", err.toString());
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
	
    protected DValue getPrimaryKeyValue(DeliaImmutable immut) {
		PrimaryKey pk = structType.getPrimaryKey();
		DValue pkval = immut.internalDValue().asStruct().getField(pk.getFieldName());
		return pkval;

    }
	protected List<T> createImmutList(ResultValue res) {
		List<T> list = new ArrayList<>();
		for(DValue dval: res.getAsDValueList()) {
			list.add(createImmutFromDVal(dval));
		}
		return list;
	}
	protected abstract T createImmutFromDVal(DValue dval);
	
	protected T createImmut(ResultValue res) {
		DValue dval = res.getAsDValue();
		return createImmutFromDVal(dval);
	}
	protected ResultValue doQuery(String src) {
		log.log("src: %s", src);
		//use child session for thread-safety and isolation
		DeliaSession session = mainSession.createChildSession();
		ResultValue res = delia.continueExecution(src, session);
		if (! res.ok) {
			DeliaExceptionHelper.throwError("dao-error", "Query failed: %s", src);
		}
		return res;
	}
	
	
	protected T doFindById(int id) {
		String src = String.format("%s[%s]", typeName, id);
		ResultValue res = doQuery(src);
		return createImmut(res);
	}
	protected List<T> doFindAll() {
		String src = String.format("%s[true]", typeName);
		ResultValue res = doQuery(src);
		return createImmutList(res);
	}
	
	protected DValue doInsert(T entity) {
		String src = String.format("insert %s {}", typeName);
		InsertExtraInfo extraInfo = new InsertExtraInfo();
		doInsertOrUpdate(entity, src, extraInfo);
		return extraInfo.generatedSerialValue;
	}
	protected boolean canSetSerialId(T entity, DValue serialVal) {
		if (entity instanceof DeliaEntity && serialVal != null) {
			return true;
		}
		return false;
	}
	
	protected int doUpdate(T entity) {
		DValue pkval = getPrimaryKeyValue(entity);
		String src = String.format("update %s[%s] {}", typeName, pkval.asString());
		ResultValue res = doInsertOrUpdate(entity, src, null);
		Integer updateCount = (Integer) res.val;
		return updateCount;
	}
	protected int doUpsert(T entity) {
		DValue pkval = getPrimaryKeyValue(entity);
		String src = String.format("upsert %s[%s] {}", typeName, pkval.asString());
		ResultValue res = doInsertOrUpdate(entity, src, null);
		Integer updateCount = (Integer) res.val;
		return updateCount;
	}
	
	protected void doDelete(T entity) {
		DValue pkval = getPrimaryKeyValue(entity);
		String src = String.format("delete %s[%s]", typeName, pkval.asString());
		ResultValue res = doInsertOrUpdate(entity, src, null);
	}
	protected void doDeleteAll() {
		String src = String.format("delete %s[true]", typeName);
		ResultValue res = doInsertOrUpdate(null, src, null);
	}
	
	//--derived classes can override these if necessary--
	public List<T> findAll() {
		return doFindAll();
	}
	//not insert because may or may not return generated id
	public int update(T entity) {
		return doUpdate(entity);
	}
	public int upsert(T entity) {
		return doUpsert(entity);
	}
	public void delete(T entity) {
		doDelete(entity);
	}

}