package com.ssplugins.shadow3.execute;

import com.squareup.javapoet.JavaFile;
import com.ssplugins.shadow3.Shadow;
import com.ssplugins.shadow3.commons.ShadowCommons;
import com.ssplugins.shadow3.compile.JavaGen;
import com.ssplugins.shadow3.exception.ShadowException;
import com.ssplugins.shadow3.parsing.ShadowParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Execute {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            log("Enter a path to a Shadow (.shd) file.");
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            log("File does not exist.");
            return;
        }
        if (!file.getName().toLowerCase().endsWith(".shd")) {
            log("Please enter a path to a Shadow (.shd) file.");
            return;
        }
        
        // Get arguments and flags.
        List<Object> params = new ArrayList<>(Arrays.asList((String[]) args));
        params.remove(0);
        Set<String> flags = new HashSet<>(5);
        Iterator<Object> it = params.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (s.startsWith("--")) {
                s = s.substring(2);
                if (!s.isEmpty()) flags.add(s);
                it.remove();
            }
        }
        
        boolean compile = false;
        boolean raw = false;
        
        // Process flags
        if (flags.contains("e")) {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                e.printStackTrace();
                try {
                    System.in.read();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            });
        }
        if (flags.contains("c")) {
            compile = true;
        }
        if (flags.contains("j")) {
            raw = true;
        }
        
        // Run script
        ShadowParser parser = new ShadowParser(ShadowCommons.create(file));
        Shadow shadow = parser.parse(file);
        boolean willCompile = compile;
        boolean rawOutput = raw;
        shadow.firstBlock("main").ifPresent(block -> {
            if (!(willCompile || rawOutput)) block.run(params);
            else {
                JavaFile javaFile = shadow.generateFile();
                if (rawOutput) {
                    File output = toExt(file, ".java");
                    try (FileWriter writer = new FileWriter(output)) {
                        javaFile.writeTo(writer);
                    } catch (IOException e) {
                        throw new ShadowException(e);
                    }
                }
                if (willCompile) {
                    try {
                        File dir = JavaGen.compile(javaFile);
                        File manifest = JavaGen.createManifest(dir, javaFile);
                        File output = toExt(file, ".jar");
                        int code = JavaGen.packageJar(dir, manifest, output);
                        if (code == 0) log("Compiled successfully.");
                        else log("Error while compiling. (Status code " + code + ")");
                        if (!deleteFully(dir)) System.out.println("Unable to delete temp directory: " + dir.getPath());
                    } catch (IOException | InterruptedException e) {
                        throw new ShadowException(e);
                    }
                }
            }
        });
    }
    
    private static void log(Object msg) {
        System.out.println(msg);
    }
    
    private static String getName(File file) {
        int index = file.getName().lastIndexOf('.');
        return file.getName().substring(0, index);
    }
    
    private static File toExt(File file, String ext) {
        String name = file.getName();
        return new File(file.getParent(), name.substring(0, name.lastIndexOf('.')) + ext);
    }
    
    private static boolean deleteFully(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File file : files) {
            if (file.isDirectory() && !deleteFully(dir)) return false;
            if (!file.delete()) return false;
        }
        return dir.delete();
    }
    
}
