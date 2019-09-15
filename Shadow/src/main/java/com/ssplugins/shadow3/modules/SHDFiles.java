package com.ssplugins.shadow3.modules;

import com.squareup.javapoet.MethodSpec;
import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.compile.Code;
import com.ssplugins.shadow3.compile.TypeChecker;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.def.Returnable;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.exception.ShadowParseError;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.section.ShadowSection;
import com.ssplugins.shadow3.section.ShadowString;
import com.ssplugins.shadow3.util.Range;

import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SHDFiles extends ShadowAPI {
    
    private ShadowContext context;
    
    @Override
    public void loadInto(ShadowContext context) {
        this.context = context;
        context.setName("files");
        callAnnotatedMethods();
        this.context = null;
    }
    
    private <T extends AutoCloseable> T closeLater(T t, Scope scope) {
        scope.addCallback(() -> {
            try {
                t.close();
            } catch (Exception e) {
                throw new ShadowException(e);
            }
        });
        return t;
    }
    
    @Entity
    void keywordFile() {
        KeywordType file = new KeywordType("file", new Range.Single(1));
        file.setAction((keyword, stepper, scope) -> {
            String path = keyword.getString(0, scope);
            return new File(path);
        });
        file.setReturnable(Returnable.of(File.class));
        file.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, String.class);
            String tmp = c.getScope().nextTemp();
            method.addStatement("$T $L = new $T($L)", File.class, tmp, File.class, section.getGeneration(c, type, method));
            return Code.plain(tmp);
        });
        context.addKeyword(file);
    }
    
    @Entity
    void keywordExists() {
        KeywordType exists = new KeywordType("exists", new Range.Single(1));
        exists.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            return f.exists();
        });
        exists.setReturnable(Returnable.of(Boolean.class));
        exists.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, File.class);
            return Code.format("$L.exists()", section.getGeneration(c, type, method));
        });
        context.addKeyword(exists);
    }
    
    @Entity
    void keywordOpen() {
        KeywordType open = new KeywordType("open", new Range.MinMax(1, 2));
        open.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file object.");
            try {
                String mode = "r";
                if (keyword.getArguments().size() > 1) {
                    mode = keyword.getStringSection(1).getValue();
                    mode = mode.toLowerCase();
                    if (mode.endsWith("+")) {
                        mode = mode.substring(0, mode.length() - 1);
                        if (!f.exists() && !f.createNewFile()) {
                            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(0), "File did not exist and was unable to be created.");
                        }
                    }
                }
                if (mode.equals("r")) return closeLater(new FileReader(f), scope);
                if (mode.equals("w")) return closeLater(new FileWriter(f), scope);
                if (mode.equals("a")) return closeLater(new FileWriter(f, true), scope);
                throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(1), "Unknown file mode.");
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        open.setReturnable((keyword, scope) -> {
            List<ShadowSection> args = keyword.getArguments();
            if (args.size() == 1) return FileReader.class;
            ShadowString section = keyword.getStringSection(1);
            String mode = section.getValue().toLowerCase();
            if (mode.equals("r")) return FileReader.class;
            if (mode.equals("w")) return FileWriter.class;
            if (mode.equals("a")) return FileWriter.class;
            throw new ShadowParseError(keyword.getLine(), keyword.argumentIndex(1), "Unknown file mode.");
        });
        open.setGenerator((c, keyword, type, method) -> {
            List<ShadowSection> args = keyword.getArguments();
            TypeChecker.require(c.getScope(), args.get(0), File.class);
            Code fileGen = args.get(0).getGeneration(c, type, method);
            String tmp = c.getScope().nextTemp();
            Code tmpCode = Code.plain(tmp);
            if (args.size() == 1) {
                method.addStatement("$T $L = new $T($L)", FileReader.class, tmp, FileReader.class, fileGen);
                return tmpCode;
            }
            ShadowString section = keyword.getStringSection(1);
            String mode = section.getValue().toLowerCase();
            boolean create = false;
            if (mode.endsWith("+")) {
                mode = mode.substring(0, mode.length() - 1);
                create = true;
            }
            if (mode.equals("r")) {
                method.addStatement("$T $L = new $T($L)", FileReader.class, tmp, FileReader.class, fileGen);
                return tmpCode;
            }
            if (mode.equals("w")) {
                method.addStatement("$T $L = new $T($L)", FileWriter.class, tmp, FileWriter.class, fileGen);
                return tmpCode;
            }
            if (mode.equals("a")) {
                method.addStatement("$T $L = new $T($L, true)", FileWriter.class, tmp, FileWriter.class, fileGen);
                return tmpCode;
            }
            throw new ShadowCodeException(keyword.getLine(), keyword.argumentIndex(1), "Unknown file mode.");
        });
        context.addKeyword(open);
    }
    
    @Entity
    void keywordClose() {
        KeywordType close = new KeywordType("close", new Range.Single(1));
        close.setAction((keyword, stepper, scope) -> {
            Closeable closeable = keyword.getArgument(0, Closeable.class, scope, "Argument must be closeable.");
            try {
                closeable.close();
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
        });
        close.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Closeable.class);
            String name = c.getComponentName("close");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(void.class)
                                            .addParameter(Closeable.class, "c")
                                            .beginControlFlow("try")
                                            .addStatement("c.close()")
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while closing reader/writer.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            method.addStatement("$L($L)", name, section.getGeneration(c, type, method));
            return null;
        });
        close.setStatementMode(true);
        context.addKeyword(close);
    }
    
    @Entity
    void keywordReadAll() {
        KeywordType readAll = new KeywordType("read_all", new Range.Single(1));
        readAll.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try {
                return new String(Files.readAllBytes(f.toPath()));
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        readAll.setReturnable(Returnable.of(String.class));
        readAll.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, File.class);
            String name = c.getComponentName("readAll");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(String.class)
                                            .addParameter(File.class, "f")
                                            .beginControlFlow("try")
                                            .addStatement("return $T.readAllBytes(f.toPath())", Files.class)
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while reading file.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(readAll);
    }
    
    @Entity
    void keywordReadLine() {
        KeywordType readLine = new KeywordType("read_line", new Range.Single(1));
        readLine.setAction((keyword, stepper, scope) -> {
            Reader reader = keyword.getArgument(0, Reader.class, scope, "Argument must be a reader.");
            StringBuilder builder = new StringBuilder();
            try {
                char c;
                while ((c = (char) reader.read()) != '\n') {
                    if (c == '\r') continue;
                    builder.append(c);
                }
                return builder.toString();
            } catch (IOException e) {
                if (e instanceof EOFException && builder.length() != 0) return builder.toString();
                throw new ShadowException(e);
            }
        });
        readLine.setReturnable(Returnable.of(String.class));
        readLine.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Reader.class);
            String name = c.getComponentName("readLine");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(String.class)
                                            .addParameter(Reader.class, "reader")
                                            .addStatement("$T builder = new $T()", StringBuilder.class, StringBuilder.class)
                                            .beginControlFlow("try")
                                            .addStatement("char c")
                                            .beginControlFlow("while ((c = (char) reader.read()) != '\\n')")
                                            .addStatement("if (c == '\\r') continue")
                                            .addStatement("builder.append(c)")
                                            .endControlFlow()
                                            .addStatement("return builder.toString()")
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("if ($L instanceof $T && builder.length() != 0) return builder.toString()", tmp, EOFException.class)
                                            .addStatement("throw new $T(\"Error while getting input from reader.\", $L")
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(readLine);
    }
    
    @Entity
    void keywordReady() {
        KeywordType ready = new KeywordType("ready", new Range.Single(1));
        ready.setAction((keyword, stepper, scope) -> {
            Reader reader = keyword.getArgument(0, Reader.class, scope, "Argument must be a reader.");
            try {
                return reader.ready();
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        ready.setReturnable(Returnable.of(Boolean.class));
        ready.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Reader.class);
            String name = c.getComponentName("ready");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Boolean.class)
                                            .addParameter(Reader.class, "reader")
                                            .beginControlFlow("try")
                                            .addStatement("return reader.ready()")
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while checking reader.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(ready);
    }
    
    @Entity
    void keywordWrite() {
        KeywordType write = new KeywordType("write", new Range.LowerBound(2));
        write.setAction((keyword, stepper, scope) -> {
            Writer writer = keyword.getArgument(0, Writer.class, scope, "Argument must be a writer.");
            String content = keyword.getString(1, scope);
            try {
                writer.write(content);
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
        });
        write.setReturnable(Returnable.none());
        write.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Writer.class);
            ShadowSection content = keyword.getArguments().get(1);
            TypeChecker.require(c.getScope(), content, String.class);
            String name = c.getComponentName("write");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(void.class)
                                            .addParameter(Writer.class, "writer")
                                            .addParameter(String.class, "content")
                                            .beginControlFlow("try")
                                            .addStatement("writer.write(content)")
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while writing.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(write);
    }
    
    @Entity
    void keywordWriteLine() {
        KeywordType writeLine = new KeywordType("write_line", new Range.LowerBound(2));
        writeLine.setAction((keyword, stepper, scope) -> {
            Writer writer = keyword.getArgument(0, Writer.class, scope, "Argument must be a writer.");
            String content = keyword.getString(1, scope);
            try {
                writer.write(content + "\n");
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
        });
        writeLine.setReturnable(Returnable.none());
        writeLine.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, Writer.class);
            ShadowSection content = keyword.getArguments().get(1);
            TypeChecker.require(c.getScope(), content, String.class);
            String name = c.getComponentName("write_line");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(void.class)
                                            .addParameter(Writer.class, "writer")
                                            .addParameter(String.class, "content")
                                            .beginControlFlow("try")
                                            .addStatement("writer.write(content + \"\\n\")")
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while writing.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(writeLine);
    }
    
    @Entity
    void keywordLines() {
        KeywordType lines = new KeywordType("lines", new Range.Single(1));
        lines.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try {
                return closeLater(Files.lines(f.toPath()), scope).iterator();
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        lines.setReturnable(Returnable.of(Iterator.class));
        lines.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, File.class);
            String name = c.getComponentName("lines");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Iterator.class)
                                            .addParameter(File.class, "f")
                                            .beginControlFlow("try")
                                            .addStatement("return $T.lines(f.toPath())", Files.class)
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while writing.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(lines);
    }
    
    @Entity
    void keywordLineList() {
        KeywordType lineList = new KeywordType("line_list", new Range.Single(1));
        lineList.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try (Stream<String> lines = Files.lines(f.toPath())) {
                return lines.collect(Collectors.toList());
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        lineList.setReturnable(Returnable.of(List.class));
        lineList.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, File.class);
            String name = c.getComponentName("line_list");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Iterator.class)
                                            .addParameter(File.class, "f")
                                            .beginControlFlow("try ($T<$T> lines = $T.lines(f.toPath()))", Stream.class, String.class, Files.class)
                                            .addStatement("return lines.collect($T.toList())", Collectors.class)
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while reading file.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(lineList);
    }
    
    @Entity
    void keywordLineArray() {
        KeywordType lineArray = new KeywordType("line_array", new Range.Single(1));
        lineArray.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try (Stream<String> lines = Files.lines(f.toPath())) {
                return lines.toArray(String[]::new);
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        lineArray.setReturnable(Returnable.of(String[].class));
        lineArray.setGenerator((c, keyword, type, method) -> {
            ShadowSection section = keyword.getArguments().get(0);
            TypeChecker.require(c.getScope(), section, File.class);
            String name = c.getComponentName("line_array");
            c.checkName(name, s -> {
                String tmp = c.getScope().nextTemp();
                MethodSpec spec = MethodSpec.methodBuilder(s)
                                            .returns(Iterator.class)
                                            .addParameter(File.class, "f")
                                            .beginControlFlow("try ($T<$T> lines = $T.lines(f.toPath()))", Stream.class, String.class, Files.class)
                                            .addStatement("return lines.toArray(String[]::new)", Collectors.class)
                                            .nextControlFlow("catch ($T $L)", IOException.class, tmp)
                                            .addStatement("throw new $T(\"Error while reading file.\", $L)", IllegalStateException.class, tmp)
                                            .endControlFlow()
                                            .build();
                type.addMethod(spec);
            });
            return Code.format("$L($L)", name, section.getGeneration(c, type, method));
        });
        context.addKeyword(lineArray);
    }
    
}
