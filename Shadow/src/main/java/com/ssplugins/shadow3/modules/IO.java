package com.ssplugins.shadow3.modules;

import com.ssplugins.shadow3.api.ShadowAPI;
import com.ssplugins.shadow3.api.ShadowContext;
import com.ssplugins.shadow3.def.KeywordType;
import com.ssplugins.shadow3.exception.ShadowCodeException;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.execute.Scope;
import com.ssplugins.shadow3.util.Range;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IO extends ShadowAPI {
    
    private ShadowContext context;
    
    @Override
    public void loadInto(ShadowContext context) {
        this.context = context;
        context.setName("io");
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
            String path = keyword.getArgument(0, String.class, scope, "Argument must be a string.");
            return new File(path);
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
                    mode = keyword.getArgument(1, String.class, scope, "Argument must be a string.");
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
        context.addKeyword(open);
    }
    
    @Entity
    void keywordClose() {
        KeywordType close = new KeywordType("close", new Range.Single(1));
        close.setAction((keyword, stepper, scope) -> {
            Closeable closeable = keyword.getArgument(0, Closeable.class, scope, "Argument must be a reader.");
            try {
                closeable.close();
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
        });
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
        context.addKeyword(ready);
    }
    
    @Entity
    void keywordWrite() {
        KeywordType write = new KeywordType("write", new Range.LowerBound(2));
        write.setAction((keyword, stepper, scope) -> {
            Writer writer = keyword.getArgument(0, Writer.class, scope, "Argument must be a writer.");
            String content = keyword.getArgument(1, String.class, scope, "Argument must be a string.");
            try {
                writer.write(content);
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
        });
        context.addKeyword(write);
    }
    
    @Entity
    void keywordWriteLine() {
        KeywordType writeLine = new KeywordType("write_line", new Range.LowerBound(2));
        writeLine.setAction((keyword, stepper, scope) -> {
            Writer writer = keyword.getArgument(0, Writer.class, scope, "Argument must be a writer.");
            String content = keyword.getArgument(1, String.class, scope, "Argument must be a string.");
            try {
                writer.write(content + "\n");
            } catch (IOException e) {
                throw new ShadowException(e);
            }
            return null;
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
        context.addKeyword(lines);
    }
    
    @Entity
    void keywordLineList() {
        KeywordType lineList = new KeywordType("line_list", new Range.Single(1));
        lineList.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try {
                Stream<String> lines = Files.lines(f.toPath());
                List<String> list = lines.collect(Collectors.toList());
                lines.close();
                return list;
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        context.addKeyword(lineList);
    }
    
    @Entity
    void keywordLineArray() {
        KeywordType lineArray = new KeywordType("line_array", new Range.Single(1));
        lineArray.setAction((keyword, stepper, scope) -> {
            File f = keyword.getArgument(0, File.class, scope, "Argument must be a file.");
            try {
                Stream<String> lines = Files.lines(f.toPath());
                String[] array = lines.toArray(String[]::new);
                lines.close();
                return array;
            } catch (IOException e) {
                throw new ShadowException(e);
            }
        });
        context.addKeyword(lineArray);
    }
    
}
