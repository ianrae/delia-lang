package org.delia.core;

import org.delia.db.DBCapabilties;
import org.delia.db.DBInterfaceFactory;
import org.delia.db.DBType;
import org.delia.dbimpl.mem.impl.MemDBFactory;
import org.delia.dbimpl.mem.impl.MemDBFactoryImpl;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.ErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.log.LogFactory;
import org.delia.rule.RuleFuncFactoryImpl;
import org.delia.rule.RuleFunctionFactory;
import org.delia.runner.DeliaRunner;
import org.delia.transaction.DoNothingTransactionProvider;
import org.delia.transaction.TransactionProvider;
import org.delia.transaction.TransactionProviderImpl;
import org.delia.type.DTypeRegistry;
import org.delia.validation.ValidationRuleRunnerImpl;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;

public class FactoryServiceImpl implements FactoryService {
    protected DeliaLog log;
    protected ErrorTracker et;
    protected TimeZoneService tzSvc;
    private ConfigureService configSvc;
    private DateFormatServiceImpl fmtSvc;
    //	private int nextGeneratedRuleId = 1;
    private DValueCompareService compareSvc;
    //	private DiagnosticServiceImpl diagnosticSvc;
    private LogFactory logFactory;
    private boolean enableMEMSqlGenerationFlag; //normally false. no need with MEM. unless client code wants to see what sql would be
    protected MemDBFactory memDBFactory;

    public FactoryServiceImpl(DeliaLog log, ErrorTracker et) {
        this(log, et, null);
    }

    public FactoryServiceImpl(DeliaLog log, ErrorTracker et, LogFactory logFactory) {
        this.log = log;
        this.et = et;
        this.tzSvc = new TimeZoneServiceImpl();
        this.configSvc = new ConfigureServiceImpl(this);
        this.fmtSvc = new DateFormatServiceImpl(tzSvc);
        this.compareSvc = new DValueCompareService(this);
//		this.diagnosticSvc = new DiagnosticServiceImpl(this);
        this.logFactory = logFactory;
        this.memDBFactory = new MemDBFactoryImpl();
    }

    @Override
    public DateFormatService getDateFormatService() {
        return fmtSvc;
    }

    @Override
    public DeliaLog getLog() {
        return log;
    }

    @Override
    public ErrorTracker getErrorTracker() {
        return et;
    }

    @Override
    public TimeZoneService getTimeZoneService() {
        return tzSvc;
    }


    @Override
    public ConfigureService getConfigureService() {
        return configSvc;
    }

    //	@Override
//	public SchemaMigrator createSchemaMigrator(DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap, String defaultSchema) {
//		SchemaMigrator migrator = new SchemaMigrator(this, dbInterface, registry, varEvaluator, datIdMap, defaultSchema);
//		return migrator;
//	}
//
    @Override
    public ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry) {
        return new ScalarValueBuilder(this, registry);
    }

    //	@Override
//	public int getNextGeneratedRuleId() {
//		return this.nextGeneratedRuleId++;
//	}
//
    @Override
    public DValueCompareService getDValueCompareService() {
        return compareSvc;
    }

    //	@Override
//	public DiagnosticService getDiagnosticService() {
//		return diagnosticSvc;
//	}
    @Override
    public LogFactory getLogFactory() {
        return logFactory;
    }

    @Override
    public ValidationRunner createValidationRunner(DBInterfaceFactory dbInterface, DTypeRegistry registry, DeliaRunner deliaRunner) {
        DBCapabilties caps = dbInterface.getCapabilities();
        return new ValidationRuleRunnerImpl(this, dbInterface, registry, caps, deliaRunner);
    }

    @Override
    public RuleFunctionFactory createRuleFunctionFactory() {
        return new RuleFuncFactoryImpl(this);
    }

    //	@Override
//	public boolean getEnableMEMSqlGenerationFlag() {
//		return enableMEMSqlGenerationFlag;
//	}
//	@Override
//	public void setEnableMEMSqlGenerationFlag(boolean flag) {
//		enableMEMSqlGenerationFlag = flag;
//	}
//	@Override
//	public RuleFunctionFactory createRuleFunctionFactory() {
//		return new RuleFuncFactoryImpl(this);
//	}
    @Override
    public TransactionProvider createTransactionProvider(DBInterfaceFactory dbInterface) {
        if (dbInterface.getDBType().equals(DBType.MEM)) {
            return new DoNothingTransactionProvider(log);
        }
        return new TransactionProviderImpl(dbInterface, log);
    }

    @Override
    public MemDBFactory getMemDBFactory() {
        return memDBFactory;
    }

    @Override
    public void setMemDBFactory(MemDBFactory factory) {
        memDBFactory = factory;
    }

//	@Override
//	public MigrationService createMigrationService(DBInterfaceFactory dbInterface) {
//		return new SxMigrationServiceImpl(dbInterface, this);
//	}

}
