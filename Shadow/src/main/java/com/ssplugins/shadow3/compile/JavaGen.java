package com.ssplugins.shadow3.compile;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ssplugins.shadow3.entity.ShadowEntity;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.section.Compound;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.util.CompileScope;
import com.ssplugins.shadow3.util.OperatorTree;
import com.ssplugins.shadow3.util.Pair;

import javax.lang.model.SourceVersion;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// Helper methods for compiling Java
public class JavaGen {
    
    public static String checkName(String name) {
        if (!SourceVersion.isName(name)) throw new ShadowException("Invalid module name: " + name);
        return name;
    }
    
    public static File compile(JavaFile javaFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("No Java compiler found.");
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        File outputDir = Files.createTempDirectory("shadow_output").toFile();
        manager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(outputDir));
        StringWriter output = new StringWriter();
        List<JavaFileObject> units = Collections.singletonList(javaFile.toJavaFileObject());
        CompilationTask task = compiler.getTask(output, manager, null, null, null, units);
        if (!task.call()) throw new ShadowException("Error while compiling: " + output.toString());
        return outputDir;
    }
    
    public static File createManifest(File dir, JavaFile main) throws IOException {
        String entry = (main.packageName.isEmpty() ? "" : main.packageName + ".") + main.typeSpec.name;
        File out = new File(dir, "manifest.txt");
        String content = "Main-Class: " + entry + "\n";
        Files.write(out.toPath(), content.getBytes());
        return out;
    }
    
    public static int packageJar(File dir, File manifest, File output) throws IOException, InterruptedException {
        String home = System.getenv("JAVA_HOME");
        if (home == null) throw new ShadowException("JAVA_HOME environment variable is not defined.");
        File jdk = new File(home, "bin");
        if (!jdk.exists()) throw new ShadowException("Unable to get jdk directory from JAVA_HOME.");
        ProcessBuilder builder = new ProcessBuilder();
        builder.inheritIO();
        builder.command(new File(jdk, "jar").getPath(), "cvmf", manifest.getName(), output.getName(), "*.class");
        builder.directory(dir);
        Process process = builder.start();
        int i = process.waitFor();
        Files.move(new File(dir, output.getName()).toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return i;
    }
    
    //region Helpers
    
    public static String litArg(GenerateContext context, ShadowEntity entity, int arg, TypeSpec.Builder type, MethodSpec.Builder method) {
        return entity.getArguments().get(arg).getGeneration(context, type, method);
    }
    
    public static List<Pair<Class<?>, Integer>> parameterTypeIndex(Compound compound, CompileScope scope) {
        List<Pair<Class<?>, Integer>> types = new ArrayList<>();
        getNodes(node -> types.add(new Pair<Class<?>, Integer>(node.returnType(scope), leftmostIndex(node))), null, compound.getOpTree().getRoot());
        return types;
    }
    
    public static List<Class<?>> parameterTypes(Compound compound, CompileScope scope) {
        List<Class<?>> types = new ArrayList<>();
        getNodes(node -> types.add(node.returnType(scope)), null, compound.getOpTree().getRoot());
        return types;
    }
    
    public static int parameterIndex(Compound compound, int index) {
        List<OperatorTree.Node> nodes = new ArrayList<>();
        getNodes(nodes::add, null, compound.getOpTree().getRoot());
        return leftmostIndex(nodes.get(index));
    }
    
    private static int leftmostIndex(OperatorTree.Node node) {
        if (node instanceof OperatorTree.OpNode) {
            return leftmostIndex(node.getChildren()[0]);
        }
        if (node instanceof OperatorTree.UnaryOpNode) {
            return leftmostIndex(node.getChildren()[0]);
        }
        return ((OperatorTree.SectionNode) node).getSection().index();
    }
    
    public static int countParameters(Compound compound) {
        AtomicInteger counter = new AtomicInteger(0);
        getNodes(node -> counter.getAndIncrement(), null, compound.getOpTree().getRoot());
        return counter.get();
    }
    
    public static String literalParameters(Compound compound, GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        StringBuilder builder = new StringBuilder();
        getNodes(node -> builder.append(node.getGeneration(context, type, method)), () -> builder.append(", "), compound.getOpTree().getRoot());
        return builder.toString();
    }
    
    private static void getNodes(Consumer<OperatorTree.Node> consumer, Runnable between, OperatorTree.Node node) {
        if (node instanceof OperatorTree.OpNode) {
            if (!((OperatorTree.OpNode) node).getValue().getSymbol().equals(",")) {
                consumer.accept(node);
                return;
            }
            OperatorTree.Node[] c = node.getChildren();
            getNodes(consumer, between, c[0]);
            if (between != null) between.run();
            getNodes(consumer, between, c[1]);
            return;
        }
        consumer.accept(node);
    }
    
    public static String toArgs(List<ShadowSection> list, GenerateContext context, TypeSpec.Builder type, MethodSpec.Builder method) {
        return list.stream().map(section -> section.getGeneration(context, type, method)).collect(Collectors.joining(", "));
    }
    
    //endregion
    
    //region Code Generation
    
    public static void println(MethodSpec.Builder builder, String content) {
        builder.addStatement("$T.out.println($S)", System.class, content);
    }
    
    public static void print(MethodSpec.Builder builder, String content) {
        builder.addStatement("$T.out.print($S)", System.class, content);
    }
    
    public static void printlnValue(MethodSpec.Builder builder, String content) {
        builder.addStatement("$T.out.println($L)", System.class, content);
    }
    
    public static void printValue(MethodSpec.Builder builder, String content) {
        builder.addStatement("$T.out.print($L)", System.class, content);
    }
    
    public static void println(MethodSpec.Builder builder) {
        builder.addStatement("$T.out.println()", System.class);
    }
    
    //endregion
    
}
