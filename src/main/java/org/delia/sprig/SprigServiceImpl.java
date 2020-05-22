package org.delia.sprig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class SprigServiceImpl extends ServiceBase implements SprigService {

	public static class SynthInstanceDetails {
		public DValue mostRecentInsertedPrimaryKey;
		public DValue synId;
	}
	public static class SynthInfo {
		public String synthFieldName;
		public Map<String, SynthInstanceDetails> instanceMap = new ConcurrentHashMap<>(); 
	}
	private DTypeRegistry registry;
	private Map<String,SynthInfo> map = new ConcurrentHashMap<>();

	public SprigServiceImpl(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	@Override
	public void registerSyntheticId(String typeName, String syntheticIdName) {
		if (! registry.existsType(typeName)) {
			DeliaExceptionHelper.throwError("synthetic-id-unknown-type", "Cannot configure synthetic type for unknown type '%s'", typeName);
		}
		
		SynthInfo info = map.get(typeName);
		if (info == null) {
			info = new SynthInfo();
		}
		info.synthFieldName = syntheticIdName;
		map.put(typeName, info);
	}

	@Override
	public DValue resolveSyntheticId(String typeName, String idValue) {
		SynthInfo info = map.get(typeName);
		if (info == null) {
			return null; //not sfund
		}
		
		for(String mapKey: info.instanceMap.keySet()) {
			SynthInstanceDetails details = info.instanceMap.get(mapKey);
			if (details.synId.asString().equals(idValue)) {
				return details.mostRecentInsertedPrimaryKey;
			}
		}
		return null; 
	}

	@Override
	public boolean haveEnabledFor(String typeName) {
		SynthInfo info = map.get(typeName);
		return info != null;
	}

	@Override
	public boolean haveEnabledFor(String typeName, String syntheticIdName) {
		SynthInfo info = map.get(typeName);
		if (info == null) {
			return false;
		} else {
			 return info.synthFieldName.equals(syntheticIdName);
		}
	}

	@Override
	public void setGeneratedId(String typeName, DValue idVal) {
		SynthInfo info = map.get(typeName);
		if (info != null) {
			//info.mostRecentInsertedPrimaryKey = idVal;
		}
	}

	@Override
	public void rememberSynthId(String typeName, DValue dval, DValue generatedId, Map<String, DValue> extraMap) {
		SynthInfo info = map.get(typeName);
		if (info != null) {
			DValue keyVal = generatedId;
			if (keyVal == null) {
				TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
				keyVal = dval.asStruct().getField(pair.name);
			}
			String key = keyVal.asString();
			SynthInstanceDetails details = info.instanceMap.get(key);
			if (details == null) {
				details = new SynthInstanceDetails();
				info.instanceMap.put(key, details);
			}
			details.mostRecentInsertedPrimaryKey = keyVal;
			details.synId = extraMap.get(info.synthFieldName);
		}
	}
}
