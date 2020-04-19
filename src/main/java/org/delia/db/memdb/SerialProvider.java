package org.delia.db.memdb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.valuebuilder.ScalarValueBuilder;

public class SerialProvider extends ServiceBase {
	public static interface SerialGenerator {
		DValue generate(String key, ScalarValueBuilder scalarBuilder);
	}
	public static class IntSerialGen implements SerialGenerator {
		private Integer n = 1;

		@Override
		public DValue generate(String key, ScalarValueBuilder scalarBuilder) {
			DValue dval = scalarBuilder.buildInt(n);
			n = n + 1;
			return dval;
		}
	}
	public static class LongSerialGen implements SerialGenerator {
		private Long n = 1L;
		public static Long initialValue = null;
		@Override
		public DValue generate(String key, ScalarValueBuilder scalarBuilder) {
			if (initialValue != null) {
				n = initialValue.longValue();
				initialValue = null; //only use once
			}
			DValue dval = scalarBuilder.buildLong(n);
			n = n + 1L;
			return dval;
		}
	}
	public static class StringSerialGen implements SerialGenerator {
		private Long n = 1L;

		@Override
		public DValue generate(String key, ScalarValueBuilder scalarBuilder) {
			DValue dval = scalarBuilder.buildString(n.toString());
			n = n + 1L;
			return dval;
		}
	}

	private Map<String,SerialGenerator> map = new ConcurrentHashMap<>(); //key, nextId values
	private DTypeRegistry registry;
	private ScalarValueBuilder scalarBuilder;
	
	
	public SerialProvider(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}

	public DValue generateSerialValue(DStructType structType, TypePair pair) {
		String key = genKey(structType, pair);
		
		SerialGenerator gen = map.get(key);
		if (gen == null) {
			gen = createGen(pair); 
			map.put(key,  gen);
		}
		
		return gen.generate(key, scalarBuilder);
	}

	private SerialGenerator createGen(TypePair pair) {
		switch(pair.type.getShape()) {
		case INTEGER:
			return new IntSerialGen();
		case LONG:
			return new LongSerialGen();
		case STRING:
			return new StringSerialGen();
		default:
			return null; //error!! TODO fix
		}
	}

	private String genKey(DStructType structType, TypePair pair) {
		return String.format("%s.%s", structType, pair.name);
	}

	public void setRegistry(DTypeRegistry registry) {
		this.registry = registry;
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}

}
