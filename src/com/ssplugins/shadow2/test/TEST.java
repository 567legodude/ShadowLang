package com.ssplugins.shadow2.test;

import com.ssplugins.shadow2.Debug;
import com.ssplugins.shadow2.Shadow;
import com.ssplugins.shadow2.ShadowParser;
import com.ssplugins.shadow2.element.ShadowElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TEST {
	
	public static void main(String[] args) throws IOException {
		Debug.setEnabled(true);
		
		InputStream stream = TEST.class.getResourceAsStream("/com/ssplugins/shadow2/test/testy.shd");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		
		ShadowParser parser = new ShadowParser();
		Shadow shadow = parser.parse(lines);
		List<ShadowElement> elements = shadow.getElements();
		elements.forEach(shadowElement -> {
			Debug.log(shadowElement.getClass().getSimpleName());
		});
	}
	
}
