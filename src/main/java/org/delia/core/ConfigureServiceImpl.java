package org.delia.core;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class ConfigureServiceImpl implements ConfigureService {
	private static final String TIMEZONE = "timezone";
	private static final String LOAD_FKS = "loadFKs";
	private static final String SYNTHETIC_IDS = "synthetic_id";
	private FactoryService factorySvc;
	private boolean populateFKsFlag = false;

	public ConfigureServiceImpl(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
	}

	@Override
	public boolean validate(String varName) {
		List<String> all = Arrays.asList(TIMEZONE, LOAD_FKS, SYNTHETIC_IDS);
		return all.contains(varName);
	}

	@Override
	public void execute(ConfigureStatementExp exp, DTypeRegistry registry, Object sprigSvcParam) {
		switch(exp.varName) {
		case TIMEZONE:
		{
			String tzName = exp.value.strValue();
			ZoneId tz = null;
			try {
				tz = ZoneId.of(tzName);
			} catch (Exception e) {
				DeliaExceptionHelper.throwError("bad-timezone", e.getMessage());
			}
			if (tz == null) {
				throwError("configure-error-timezone", "unknown timezone: " + tzName);
			} else {
				factorySvc.getLog().log("configure setting timezone=%s (%s)", tzName, tz.getId());
				TimeZoneService tzSvc = factorySvc.getTimeZoneService();
				tzSvc.setDefaultTimeZone(tz);
			}
		}
			break;
		case LOAD_FKS:
		{
			BooleanExp bexp = (BooleanExp) exp.value;
			this.populateFKsFlag = bexp.val;
		}
			break;
		case SYNTHETIC_IDS:
		{
			SprigService sprigSvc = (SprigService) sprigSvcParam;
			String typeName = exp.getPrefix();
			String name = exp.value.strValue();
			sprigSvc.registerSyntheticId(typeName, name);
		}
			break;
		default:
		{
			throwError("configure-error-unknown-var", "unknown configure variable: " + exp.varName);
		}
		}
	}
	
	void throwError(String id, String msg) {
		DeliaError err = new DeliaError(id, msg);
		throw new DeliaException(err);
	}

	@Override
	public boolean isPopulateFKsFlag() {
		return populateFKsFlag;
	}

	@Override
	public void setPopulateFKsFlag(boolean b) {
		populateFKsFlag = b;
	}

}
