package org.delia.dataimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.api.DeliaSession;
import org.delia.core.ServiceBase;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.dval.TypeDetector;
import org.delia.runner.inputfunction.LineObj;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StringUtil;

public class ImportToool extends ServiceBase {

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
					String tlang = pair.type.getName().equalsIgnoreCase("STRING_SHAPE") ? "trim()" : "";
					sc.o("  %s -> o.%s using { %s }\n", column, pair.name, tlang);
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
			
			return sc.toString();
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
			
			return sc.toString();
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