package org.delia.db.schema.modify;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.delia.assoc.DatIdMap;
import org.delia.assoc.DatIdMapHelper;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.schema.DatMapBuilder;
import org.delia.db.schema.SchemaMigrator;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.runner.QueryResponse;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBExecutor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SxDatMapBuilderImpl extends RegAwareServiceBase implements DatMapBuilder {

	private DBExecutor zexec;

	public SxDatMapBuilderImpl(DTypeRegistry registry, FactoryService factorySvc, DBExecutor zexec) {
		super(registry, factorySvc);
		this.zexec = zexec;
	}

	@Override
	public DatIdMap buildDatIdMapFromDBFingerprint() {
		String fingerprint = calcDBFingerprint();
		log.log("DB fingerprint: " + fingerprint);
		SchemaDefinition schema = createSchemaDefFromJSON(fingerprint);
		return buildDatIdMap(schema);
	}
	
	public SchemaDefinition parseJson(String fingerprintJson) {
		SchemaDefinition schema = createSchemaDefFromJSON(fingerprintJson);
		return schema;
	}

	public String calcDBFingerprint() {
		//TODO: query just single record (most recent);
		FilterExp filter = new FilterExp(99, new BooleanExp(true)); //query all
		QuerySpec spec = new QuerySpec();
		spec.queryExp = new QueryExp(99, new IdentExp(SchemaMigrator.SCHEMA_TABLE), filter, null);
		HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(zexec.getDbInterface(), registry);
		QueryResponse qresp = querySvc.execQuery(spec.queryExp, zexec);
		//TODO: should specify orderby id!!

		if (qresp.emptyResults()) {
			return "";
		}

		//there may be multiple rows
		int n = qresp.dvalList.size();
		DValue dval = qresp.dvalList.get(n - 1); //last one
		return dval.asStruct().getField("fingerprint").asString();
	}


	private DatIdMap buildDatIdMap(SchemaDefinition schema) {
		DatIdMap datMap = new DatIdMap();
		for(SxTypeInfo sctype: schema.types) {
			for(SxFieldInfo ff: sctype.flds) {
				DType dtype = registry.getType(ff.t);
				if (dtype != null && dtype.isStructShape()) {
					String key = DatIdMapHelper.createKey(sctype.nm, ff.f);
					int datId = ff.datId;
					if (datId != 0) {
						datMap.put(key, datId);
						log.log(String.format("DAT map: %s %d", key, datId));
					}
				}
			}
		}
		return datMap;
	}

	private SchemaDefinition createSchemaDefFromJSON(String json) {
		if (StringUtils.isEmpty(json)) {
			return new SchemaDefinition();
		}
		
		SchemaDefinition def = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			def = mapper.readValue(json, SchemaDefinition.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return def;
	}

}
