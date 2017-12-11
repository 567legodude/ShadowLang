package com.ssplugins.shadow2;

import com.ssplugins.shadow2.exceptions.ShadowAPIException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShadowParser {
	
	private List<ShadowAPI> apis = new ArrayList<>();
	
	public ShadowParser() {}
	
	public void addApi(ShadowAPI api) {
		if (api != null) apis.add(api);
	}
	
	public Shadow parse(List<String> content) {
		List<String> lines = new ArrayList<>(content);
		ShadowContext context = new ShadowContext(apis);
		context.getLineParsers().forEach(parser -> lines.replaceAll(parser::parse));
		apis.forEach(api -> api.peekLines(lines));
		
	}
	
	public Shadow parse(String[] lines) {
		return parse(Arrays.asList(lines));
	}
	
	public Shadow parse(String content) {
		return parse(content.split("\\r?\\n"));
	}
	
	public Shadow parse(File file) throws IOException {
		List<String> out = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			out.add(line);
		}
		reader.close();
		return parse(out);
	}
	
	public Shadow parseFileSafe(File file) {
		try {
			return parse(file);
		} catch (IOException e) {
			e.printStackTrace();
			return Shadow.empty();
		}
	}
	
}
