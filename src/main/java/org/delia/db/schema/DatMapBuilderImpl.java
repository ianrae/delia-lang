package org.delia.db.schema;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.assoc.DatIdMapHelper;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.runner.QueryResponse;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBExecutor;

public class DatMapBuilderImpl extends RegAwareServiceBase implements DatMapBuilder {

	private DBExecutor zexec;
	private SchemaMigrator schemaMigrator;

	public DatMapBuilderImpl(DTypeRegistry registry, FactoryService factorySvc, DBExecutor zexec, SchemaMigrator schemaMigrator) {
		super(registry, factorySvc);
		this.zexec = zexec;
		this.schemaMigrator = schemaMigrator;
	}

	@Override
	public DatIdMap buildDatIdMapFromDBFingerprint() {
		String fingerprint = calcDBFingerprint();
		log.log("DB fingerprint: " + fingerprint);
		return buildDatIdMap(fingerprint);
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


	private DatIdMap buildDatIdMap(String fingerprint) {
		DatIdMap datMap = new DatIdMap();
		List<SchemaType> list = schemaMigrator.parseFingerprint(fingerprint);
		for(SchemaType sctype: list) {
			List<FieldInfo> fieldInfoL = schemaMigrator.parseFields(sctype);
			for(FieldInfo ff: fieldInfoL) {
				DType dtype = registry.getType(ff.type);
				if (dtype != null && dtype.isStructShape()) {
					String key = DatIdMapHelper.createKey(sctype.typeName, ff.name);
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


}
