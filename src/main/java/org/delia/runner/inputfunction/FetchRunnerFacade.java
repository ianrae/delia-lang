package org.delia.runner.inputfunction;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class FetchRunnerFacade implements FetchRunner {

	private FetchRunner inner;
	private Log log;
	private ExternalDataLoader externalLoader;
	private ImportMetricObserver metricsObserver;
	private ImportSpec ispec;

	public FetchRunnerFacade(FactoryService factorySvc, FetchRunner fr, ExternalDataLoader externalLoader, ImportSpec ispec, ImportMetricObserver metricsObserver) {
		this.inner = fr;
		this.log = factorySvc.getLog();
		this.externalLoader = externalLoader;
		this.ispec = ispec;
		this.metricsObserver = metricsObserver;
	}

	@Override
	public QueryResponse load(DRelation drel) {
		log.logDebug("FRFFFFFFFFFFFFFFF1");
		QueryResponse qresp = inner.load(drel);
		
		if (!qresp.ok) {
			return qresp;
		} else if (externalLoader != null && CollectionUtils.isEmpty(qresp.dvalList)) {
			qresp = externalLoader.queryObjects(drel);
			log.logDebug("aaaaaaaaaaaFRFFFFFFFFFFFFFFF1");
			return qresp;
		} else {
			return qresp;
		}
	}

	@Override
	public boolean queryFKExists(DStructType owningType, String subject, DRelation drel) {
		log.logDebug("FRFFFFFFFFFFFFFFF2");
		return inner.queryFKExists(owningType, subject, drel);
	}

	@Override
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal) {
		log.logDebug("FRFFFFFFFFFFFFFFF3");
		return inner.loadFKOnly(typeName, fieldName, keyVal);
	}

	@Override
	public boolean queryFKExists(DRelation drel) {
		log.logDebug("bbbbbbbbbbFRFFFFFFFFFFFFFFF1");
		boolean exists = inner.queryFKExists(drel);
		
		if (! exists && externalLoader != null) {
			QueryResponse qresp = externalLoader.queryFKsExist(drel);
			if (qresp != null && ! qresp.ok) {
				//log errors
				return false;
			}
			exists = CollectionUtils.isNotEmpty(qresp.dvalList);
			if (exists) {
				metricsObserver.onLoadedExternally(ispec, qresp.dvalList.size());
			}
			return exists;
		}
		
		return exists;
	}

	@Override
	public List<DValue> queryFKs(DStructType owningType, String subject, DRelation drel) {
		log.logDebug("FRFFFFFFFFFFFFFFF2");
		return inner.queryFKs(owningType, subject, drel);
	}

}