//package org.delia.migrationparser;
//
//import org.apache.commons.lang3.StringUtils;
//import org.delia.DeliaSession;
//import org.delia.db.DBType;
//import org.delia.platdelia.core.FileSpec;
//import org.delia.platdelia.delia.DeliaService;
//import org.delia.platdelia.delia.DeliaSourceGenerator;
//import org.delia.platdelia.error.PDExceptionHelper;
//import org.delia.platdelia.migration.parser.MigrationParser;
//import org.delia.platdelia.migration.parser.Token;
//import org.delia.platdelia.migration.parser.ast.AST;
//import org.delia.type.DTypeRegistry;
//import org.delia.util.TextFileWriter;
//import org.platframework.component.ComponentBase;
//import org.platframework.component.ComponentContext;
//import org.platframework.component.ComponentLocator;
//import org.platframework.core.log.Log;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static java.util.Objects.isNull;
//
//public class MigrationFileService extends ComponentBase {
//    private DeliaService deliaSvc;
//    private Log log;
//    private String outDir = "C:/tmp/k"; //TODO make configurable
//    private DeliaSession finalSess;
//
//    public static class MigrationInfo {
//        List<AST> asts = new ArrayList<>();
//        String additionSrc;
//    }
//
//    @Override
//    public void initComponent(ComponentLocator loc, ComponentContext ctx) {
//        this.log = ctx.getLog();
//        this.deliaSvc = loc.get(DeliaService.class);
//    }
//
//    public List<String> buildFileList(String dir) throws IOException {
//        List<String> migrationFiles = findFiles(dir);
//        return migrationFiles;
//    }
//
//    public void run(List<FileSpec> migrationFiles) throws IOException {
//        //later as optimization check file dates. if all older than _delia.delia then just use that
//        //will helps tests be fast
//        log.log("Migration: %d files", migrationFiles.size());
//
//        DeliaSession sess = null;
//        String deliaSrc = null;
//        int index = 0;
//        for (FileSpec fspec : migrationFiles) {
//            MigrationInfo info = parseMigrationFile(fspec);
//            //apply asts to sess. error if sess is null.
//            //apply addition to sess. ok if sess is null
//            MigrationContext ctx = new MigrationContext();
//            sess = applyMigration(info, sess, ctx);
//
//            //render sess to deliaSrc
//            if (index == 0) {
//                deliaSrc = info.additionSrc;
//            } else {
//                deliaSrc = renderToDelia(sess, ctx);
//                if (!StringUtils.isEmpty(info.additionSrc)) {
//                    deliaSrc += "\n";
//                    deliaSrc += info.additionSrc;
//                }
//
//                //create new sess with it
//                sess = deliaSvc.initDelia(DBType.MEM, deliaSrc);
//                finalSess = sess;
//            }
//            index++;
//        }
//
//        //store as _delia.delia
//        writeFile(deliaSrc);
//    }
//
//    public DeliaSession getFinalSession() {
//        return finalSess;
//    }
//
//    private void writeFile(String deliaSrc) {
//        TextFileWriter w = new TextFileWriter();
//        String path = String.format("%s/%s", outDir, "_delia.delia");
//        log.log("write delia file: %s", path);
//        w.writeFile(path, Collections.singletonList(deliaSrc));
//    }
//
//    private String renderToDelia(DeliaSession sess, MigrationContext ctx) {
//        DeliaSourceGenerator generator = new DeliaSourceGenerator(ctx);
//        return generator.render(sess.getRegistry());
//    }
//
//    private DeliaSession applyMigration(MigrationInfo info, DeliaSession sess, MigrationContext ctx) {
//        DTypeRegistry registry = sess == null ? null : sess.getRegistry();
//        if (isNull(registry)) {
//            if (!info.asts.isEmpty()) {
//                PDExceptionHelper.throwException(log, "ALTERATIONS not allowed on first migration. %d alterations found", info.asts.size());
//            }
//            if (StringUtils.isEmpty(info.additionSrc)) {
//                PDExceptionHelper.throwException(log, "First migration must have ADDITIONS. none found");
//            }
//
//            DeliaSession newSess = deliaSvc.initDelia(DBType.MEM, info.additionSrc);
//            return newSess;
//        } else {
//            for (AST ast : info.asts) {
//                doMigration(registry, ast, ctx);
//            }
//            return sess;
//        }
//    }
//
//    private void doMigration(DTypeRegistry registry, AST ast, MigrationContext ctx) {
//        ast.applyMigration(registry, ctx);
//    }
//
//    private List<String> findFiles(String dir) {
//        List<File> files = new ArrayList<>();
//        try {
//            //TODO: This only works in app. need another solution for IDE
//            URI uri = getClass().getResource("dir").toURI();
//            Path dirPath = Paths.get(uri);
//            Files.list(dirPath)
//                    .forEach(p -> System.out.println("* " + p));
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//        return files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList());
//    }
//
//    private MigrationInfo parseMigrationFile(FileSpec fspec) throws IOException {
//        MigrationParser parser = new MigrationParser(log);
//        String src = deliaSvc.loadDelia(fspec);
//        MigrationInfo info = new MigrationInfo();
//        List<Token> tokens = parser.parseIntoTokens(src);
//        if (tokens != null) {
//            info.asts = parser.parseIntoAST(tokens);
//        }
//        info.additionSrc = parser.findAdditions(src);
//        return info;
//    }
//
//}
