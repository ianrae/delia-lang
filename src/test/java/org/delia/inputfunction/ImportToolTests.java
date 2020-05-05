package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ImportLevel;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBHelper;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.dval.TypeDetector;
import org.delia.log.LogLevel;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class ImportToolTests  extends NewBDDBase {
	
	public static class MyExternalDataLoader extends ServiceBase implements ExternalDataLoader {

		private DeliaSession externalSession;
		private DTypeRegistry externalRegistry;
		private DBInterface externalDBInterface;
		private DoNothingVarEvaluator varEvaluator;

		public MyExternalDataLoader(FactoryService factorySvc, DeliaSession externalSession) {
			super(factorySvc);
			this.externalSession = externalSession;
			this.externalRegistry = externalSession.getExecutionContext().registry;
			this.externalDBInterface = externalSession.getDelia().getDBInterface();
			this.varEvaluator = new DoNothingVarEvaluator(); //TODO fix later. what should we use?
		}

		@Override
		public QueryResponse queryFKsExist(DRelation drel) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public QueryResponse queryObjects(DRelation drel) {
			QuerySpec spec = buildQuery(drel);
			
			QueryResponse qresp = null;
			QueryContext qtx = new QueryContext();
			DBAccessContext dbctx = new DBAccessContext(externalRegistry, varEvaluator);
			try(DBExecutor dbexecutor = externalDBInterface.createExector(dbctx)) {
				qresp = dbexecutor.executeQuery(spec, qtx);
			} catch (Exception e) {
				DBHelper.handleCloseFailure(e);
			}

			return qresp;
		}
		
		private QuerySpec buildQuery(DRelation drel) {
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			QueryExp exp;
			if (drel.isMultipleKey()) {
				DType relType = externalRegistry.getType(drel.getTypeName());
				exp = builderSvc.createInQuery(drel.getTypeName(), drel.getMultipleKeys(), relType);
			} else {
				exp = builderSvc.createPrimaryKeyQuery(drel.getTypeName(), drel.getForeignKey());
			}
			QuerySpec spec = builderSvc.buildSpec(exp, varEvaluator);
			return spec;
		}
	}
	
	public static class ImportGroupBuilder extends ServiceBase {
		private List<GroupPair> groupL = new ArrayList<>();

		public ImportGroupBuilder(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		public void addImport(String inputFunctionName, LineObjIterator iter) {
			GroupPair pair = new GroupPair();
			pair.inputFnName = inputFunctionName;
			pair.iter = iter;
			this.groupL.add(pair);
		}

		public List<GroupPair> getGroupL() {
			return groupL;
		}
	}
	
	
	public static class ImportToool extends ServiceBase {

		private DeliaSession session;

		public ImportToool(DeliaSession session) {
			super(session.getDelia().getFactoryService());
			this.session = session;
		}
		
		public String generateInputFunctionSourceCode(String typeName, String path) {
			DStructType structType = (DStructType) getType(typeName); 
			CSVFileLoader loader = new CSVFileLoader(path);
			
			StrCreator sc = new StrCreator();
			String fnName = StringUtil.lowify(typeName);
			sc.o("input function %s(%s o) {\n", fnName, typeName);
			
			List<String> columns = readHeaderColumns(loader);
//			List<String> save = new ArrayList<>(columns);
			Map<String,String> usedMap = new HashMap<>();
			for(TypePair pair: structType.getAllFields()) {
				String column = findColumn(pair, columns);
				if (column != null) {
					sc.o("  %s -> o.%s using { trim() }\n", column, pair.name);
					columns.remove(column);
					usedMap.put(pair.name, "");
				}
			}
			
			//for remaining columns
			for(String col: columns) {
				sc.o("  %s -> ? using { trim() }\n", col);
			}
			
			boolean b = false;
			for(TypePair pair: structType.getAllFields()) {
				if (! usedMap.containsKey(pair.name)) {
					if (!b) {
						b = true;
						sc.o("//unused fields:\n");
					}
					sc.o(" %s\n", pair.name);
				}
			}
			sc.o("}");
			
			return sc.str;
		}
		
		public String generateDeliaStructSourceCode(String typeName, String path, boolean addLineFeed) {
			CSVFileLoader loader = new CSVFileLoader(path);
			
			StrCreator sc = new StrCreator();
			String lf = addLineFeed ? "\n" : "";
			sc.o("type %s struct {%s", StringUtil.uppify(typeName), lf);
			
			//TODO: detect type
			List<String> columns = readHeaderColumns(loader);
			List<String> types = this.detectColumnTypes(loader, 5);
			
			ListWalker<String> walker = new ListWalker<>(columns);
			int index = 0;
			while(walker.hasNext()) {
				String s = walker.next();
				String type = types.get(index);
				sc.o("    %s %s", s, type);
				if (!walker.addIfNotLast(sc, "," + lf)) {
					sc.o(lf);
				}
				index++;
			}
			
			sc.o("} end");
			
			return sc.str;
		}

		private DStructType getType(String typeName) {
			DType dtype = session.getExecutionContext().registry.getType(typeName);
			if (dtype == null || ! dtype.isStructShape()) {
				DeliaExceptionHelper.throwError("cant-find-type", "Can't find type '%s'", typeName);
			}
			DStructType structType = (DStructType) dtype;
			return structType;
		}

		private String findColumn(TypePair pair, List<String> columns) {
			for(String col: columns) {
				if (pair.name.equalsIgnoreCase(col)) {
					return col;
				}
			}
			return null;
		}

		private List<String> readHeaderColumns(CSVFileLoader loader) {
			LineObj hdrLineObj = null; //TODO support more than one later
			int numToIgnore = loader.getNumHdrRows();
			while (numToIgnore-- > 0) {
				if (!loader.hasNext()) {
					return null; //empty file
				}
				hdrLineObj = loader.next();
			}
			
			List<String> columns = new ArrayList<>();
			for(String col: hdrLineObj.elements) {
				columns.add(col.trim());
			}
			return columns;
		}
		
		private List<String> detectColumnTypes(CSVFileLoader loader, int numRowsToRead) {
			List<String> types = new ArrayList<>();
			
			while(loader.hasNext()) {
				LineObj lineObj = loader.next();
				for(String col: lineObj.elements) {
					String type = TypeDetector.detectType(col);
					types.add(type);
				}
			}
			return types;
		}
	}
	
	
	@Test
	public void testTool1Category() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		String src = createCategorySrc(false);
		buildSrc(delia, src);
		
		ImportToool tool = new ImportToool(session);
		String path = BASE_DIR + "categories.csv";
		String s = tool.generateInputFunctionSourceCode("Category", path);
		log.log("here:");
		log.log(s);
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		InputFunctionResult result = importSvc.executeImport("category", loader, ImportLevel.ONE);
		importSvc.dumpImportReport(result, observer);
	}
	
	@Test
	public void testTool1ProductSource() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		String src = createCategorySrc(false);
		buildSrc(delia, src);
		
		ImportToool tool = new ImportToool(session);
		String path = BASE_DIR + "products.csv";
		String s = tool.generateDeliaStructSourceCode("Product", path, false);
		log.log("here:");
		log.log(s);
		log.log("");
	}
	
	@Test
	public void testTool1Product() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		String src = createCategorySrc(false);
		src += " " + createProductSrc();
		buildSrc(delia, src);
		
		ImportToool tool = new ImportToool(session);
		String path = BASE_DIR + "products.csv";
		String s = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(s);
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		InputFunctionResult result = importSvc.executeImport("product", loader, ImportLevel.ONE);
		importSvc.dumpImportReport(result, observer);
	}
	
	@Test
	public void testLevel2() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		String src = createCategorySrc(false);
		src += " " + createProductSrc();
		buildSrc(delia, src);
		
		ImportToool tool = new ImportToool(session);
		String path = BASE_DIR + "products.csv";
		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(prodSrc);
		
		String path2 = BASE_DIR + "categories.csv";
		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
		log.log("here:");
		log.log(catSrc);
		
		String newSrc = prodSrc + " " + catSrc;
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(newSrc, session);
		assertEquals(true, res.ok);
		
		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
		groupBuilder.addImport("category", new CSVFileLoader(path2));
		groupBuilder.addImport("product", new CSVFileLoader(path));
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.TWO);
		for(InputFunctionResult result: resultL) {
			importSvc.dumpImportReport(result, observer);
		}
	}

	
	@Test
	public void testLevel3() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		String src = createCategorySrc(false);
		src += " " + createProductSrc();
		buildSrc(delia, src);
		
		ImportToool tool = new ImportToool(session);
		String path = BASE_DIR + "products.csv";
		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(prodSrc);
		
		String path2 = BASE_DIR + "categories.csv";
		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
		log.log("here:");
		log.log(catSrc);
		
		String newSrc = prodSrc + " " + catSrc;
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(newSrc, session);
		assertEquals(true, res.ok);
		
		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
		groupBuilder.addImport("category", new CSVFileLoader(path2));
		groupBuilder.addImport("product", new CSVFileLoader(path));
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		ExternalDataLoader externalLoader = null;
		importSvc.setExternalDataLoader(externalLoader);
		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.TWO);
		for(InputFunctionResult result: resultL) {
			importSvc.dumpImportReport(result, observer);
		}
	}

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;

	private DeliaSession session;


	@Before
	public void init() {
	}
	private void buildSrc(Delia delia, String src) {
//		String src = createCustomerSrc(which);
		delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String createCustomerSrc(int which) {

		String rule = which == 2 ? "name.len() > 4" : "";
		String src = "";
		if (which == 3) {
			src = String.format(" type Customer struct {id long primaryKey, wid int unique, name string } %s end", rule);
		} else 
		{
			src = String.format(" type Customer struct {id long primaryKey, wid int, name string } %s end", rule);
		}

		if (which == 1) {
			src += " input function foo(Customer c) { ID -> c.id, NAME -> c.name}";
		} else {
			src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, ";
			src += " NAME -> c.name using { if missing return null} }";
		}

		return src;
	}
	private String createCategorySrc(boolean inOrder) {
		if (inOrder) {
			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
			//categoryID,categoryName,description,picture
			src += String.format(" \ninput function foo(Category c) { categoryID -> c.categoryID, categoryName -> c.categoryName, description -> c.description, picture -> c.picture } ");
			src += String.format(" \nlet var1 = 55");

			return src;
		} else {
			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
			//categoryID,categoryName,description,picture
			src += String.format(" \ninput function foo(Category c) { categoryName -> c.categoryName, description -> c.description, picture -> c.picture, categoryID -> c.categoryID } ");
			src += String.format(" \nlet var1 = 55");

			return src;
		}
	}
	
	String createProductSrc() {
		String src = "type Product struct {    productID int primaryKey,    productName string,    supplierID int, relation categoryID Category optional one,    quantityPerUnit string,    unitPrice string,   ";
		src += "unitsInStock int,    unitsOnOrder int,    reorderLevel int,    discontinued int} end";
		return src;
	}
	
	
	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private LineObj createLineObj(int id, String nameStr) {
		String[] ar = { "", "33", "bob" };
		ar[0] = String.format("%d", id);
		ar[2] = nameStr;

		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
