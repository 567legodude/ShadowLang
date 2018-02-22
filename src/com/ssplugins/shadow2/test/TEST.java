package com.ssplugins.shadow2.test;

import com.ssplugins.shadow2.Debug;
import com.ssplugins.shadow2.Shadow;
import com.ssplugins.shadow2.ShadowParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TEST {
	
	public static void main(String[] args) throws IOException {
		//Debug.setEnabled(true);
		
		InputStream stream = TEST.class.getResourceAsStream("/com/ssplugins/shadow2/test/testy.shd");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		
		String s = "";
		
		ShadowParser parser = new ShadowParser();
		Shadow shadow = parser.parse(lines);
		Debug.separator();
		shadow.getElements().forEach(element -> {
			Debug.log(element.getClass().getSimpleName());
		});
		Debug.separator();
		shadow.getBlocks("test").forEach(block -> {
			shadow.run(block, null);
		});
	}
	
}
