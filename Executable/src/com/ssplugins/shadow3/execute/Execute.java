package com.ssplugins.shadow3.execute;

import com.ssplugins.shadow3.Shadow;
import com.ssplugins.shadow3.commons.ShadowCommons;
import com.ssplugins.shadow3.parsing.ShadowParser;

import java.io.File;
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
        
        // Run script
        ShadowParser parser = new ShadowParser(ShadowCommons.create(file));
        Shadow shadow = parser.parse(file);
        shadow.firstBlock("main").ifPresent(block -> {
            block.run(params);
        });
    }
    
    private static void log(Object msg) {
        System.out.println(msg);
    }
    
}
