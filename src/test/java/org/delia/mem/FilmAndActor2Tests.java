package org.delia.mem;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.dataimport.ImportToool;
import org.delia.db.DBType;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.type.DValue;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class FilmAndActor2Tests  extends NewBDDBase {
	
	@Test
	public void testTool() {
		String srcPath = IMPORT_DIR + "film-and-actor2.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		Delia delia = createDelia();
		DeliaSession session = delia.beginSession(deliaSrc);
		ImportToool tool = new ImportToool(session);

//		String csvPath = BASE_DIR + "film.csv";
//		String s = tool.generateInputFunctionSourceCode("Film", csvPath);
//		log.log(s);
		
		String csvPath = BASE_DIR + "actor.csv";
		String s = tool.generateInputFunctionSourceCode("Actor", csvPath);
		log.log(s);
	}
	
	@Test
	public void testLevel2() {
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "film.csv";
		gspec.inputFnName = "film";
		gspec.typeName = "Film";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "actor.csv";
		gspec.inputFnName = "actor";
		gspec.typeName = "Actor";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "actor-film.csv";
		gspec.inputFnName = "actorfilms";
		gspec.typeName = "Actor";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "film-and-actor2.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
		options.logDetails = true;
		List<InputFunctionResult> resultL = csvSvc.dryRunLevel2(groupList, deliaSrc, options);
		csvSvc.dumpReports(resultL);
		
		log.log("objects...");
		DeliaSession session = csvSvc.getSession();
		String query = "Film[true]";
		ResultValue res = session.getDelia().continueExecution(query, session);
		for(DValue dval: res.getAsDValueList()) {
			dumpDVal("A ",session, dval);
		}
		
		query = "Actor[true]";
		res = session.getDelia().continueExecution(query, session);
		for(DValue dval: res.getAsDValueList()) {
			dumpDVal("A2 ", session, dval);
		}
		
		log.log("and now distinct...");
		query = "Actor[true].films.distinct()";
		res = session.getDelia().continueExecution(query, session);
		for(DValue dval: res.getAsDValueList()) {
			dumpDVal("A3 ", session, dval);
		}
		
		log.log("and now distinct2...");
		query = "Actor[true].films.actors.distinct()";
		res = session.getDelia().continueExecution(query, session);
		for(DValue dval: res.getAsDValueList()) {
			dumpDVal("A3 ", session, dval);
		}
		
		//let x = Film[releaseDate='2019'].actors.orderBy('oscarsWon").limit(10).distinct()
		log.log("and now distinct3...");
		query = "Film[true].actors.orderBy('lastName').limit(3)";
		res = session.getDelia().continueExecution(query, session);
		for(DValue dval: res.getAsDValueList()) {
			dumpDVal("A3 ", session, dval);
		}

	}
	
	private String dumpDVal(String title, DeliaSession session, DValue dval) {
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		gen.includeVPrefix = false;
		DeliaGeneratePhase phase = session.getExecutionContext().generator;
		boolean b = phase.generateValue(gen, dval, "a");
		String s = StringUtil.flattenNoComma(gen.outputL);
		int pos = s.indexOf('{');
		log.log(title + s.substring(pos));
		return s;
	}
	
	
	@Test
	public void testVia() {
		String csvPath = BASE_DIR + "actor-film.csv";
		
		String srcPath = IMPORT_DIR + "film-and-actor2.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
		options.logDetails = true;
//		options.logLevel = LogLevel.DEBUG;
		InputFunctionResult result = csvSvc.dryRunLevel1(csvPath, deliaSrc, "Actor", "actorfilms", options);
		csvSvc.dumpReport(result);
	}
	
	
	private Delia createDelia() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return delia;
	}
	// --
	private final String BASE_DIR = "src/main/resources/test/film2/";
	public final String IMPORT_DIR = "src/main/resources/test/import/";

	@Before
	public void init() {
	}
	
	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}
}
