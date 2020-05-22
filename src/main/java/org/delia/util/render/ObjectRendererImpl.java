package org.delia.util.render;

import org.delia.type.DType;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ObjectRendererImpl implements ObjectRenderer {
	private ObjectMapper mapper = new ObjectMapper();
	
	public ObjectRendererImpl() {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
    	SimpleModule module = new SimpleModule();
    	module.addSerializer(DType.class, new CustomDValueSerializer());
    	mapper.registerModule(module);
	}

	@Override
	public String render(Object obj) {
		String json = null;
		try {
			
			json = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			System.out.println("OBJRENDERFAIL: " + e.getMessage());
		}
		return json;
	}

}