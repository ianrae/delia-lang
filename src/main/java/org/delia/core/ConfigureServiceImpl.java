package org.delia.core;

import org.apache.commons.lang3.StringUtils;
import org.delia.error.DeliaError;
import org.delia.lld.LLD;
import org.delia.runner.DeliaException;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public class ConfigureServiceImpl implements ConfigureService {
    public static final String SYNTHETIC_IDS_TARGET = ".synthetic_id";

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
        if (varName.endsWith(SYNTHETIC_IDS_TARGET)) {
            return true;
        }
        List<String> all = Arrays.asList(TIMEZONE, LOAD_FKS, SYNTHETIC_IDS);
        return all.contains(varName);
    }

    @Override
    public void execute(LLD.LLConfigure exp, DTypeRegistry registry, Object sprigSvcParam) {
        if (exp.configName.endsWith(SYNTHETIC_IDS_TARGET)) {
            SprigService sprigSvc = (SprigService) sprigSvcParam;
            String[] ar = exp.configName.split("\\.");
            String schema = ar.length == 3 ? ar[0] : null;
            String typeName = ar.length == 3 ? ar[1] : ar[0];
            String name = exp.dvalue.asString();
            DTypeName dtypeName = new DTypeName(schema, typeName);
            sprigSvc.registerSyntheticId(dtypeName, name);
            return;
        }

        switch (exp.configName) {
            case TIMEZONE: {
                String tzName = exp.dvalue.asString();
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
            case LOAD_FKS: {
                Boolean b = exp.dvalue.asBoolean();
                this.populateFKsFlag = b; //TODO actually implement use of this flag.
            }
            break;
            default: {
                throwError("configure-error-unknown-var", "unknown configure variable: " + exp.configName);
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
