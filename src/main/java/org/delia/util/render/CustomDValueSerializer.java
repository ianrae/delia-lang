package org.delia.util.render;

import java.io.IOException;

import org.delia.type.DType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.delia.util.DTypeNameUtil;

public class CustomDValueSerializer extends StdSerializer<DType> {

	    /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public CustomDValueSerializer() {
	        this(null);
	    }

	    public CustomDValueSerializer(Class<DType> t) {
	        super(t);
	    }

	    @Override
	    public void serialize(
	    		DType value, JsonGenerator jgen, SerializerProvider provider)
	      throws IOException, JsonProcessingException {

	        jgen.writeStartObject();
	        String tblName = DTypeNameUtil.formatForDisplay(value.getTypeName());
	        jgen.writeStringField("name", tblName);
//	        jgen.writeStringField("shape", value.getShape().name());
	        jgen.writeEndObject();
	    }
	}