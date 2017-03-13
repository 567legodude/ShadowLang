package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockHeader {
	
	private String name;
	private String[] modifiers;
	private String[] parameters;
	private int line;
	
	private BlockHeader() {}
	
	static BlockHeader parseHeader(GenericLine line) {
		BlockHeader header = new BlockHeader();
		String[] parts = line.getContent().split(" ");
		if (parts.length < 2) throw new ShadowParseException("Invalid block header on line " + line.lineNumber() + ".");
		if (parts.length == 2) {
			header.setName(parts[0]);
			header.setModifiers(new String[0]);
			header.setParameters(new String[0]);
			return header;
		}
		else {
			header.setName(parts[0]);
			List<String> mods = new ArrayList<>();
			Pattern pattern = Pattern.compile("\\(([^)]*)\\)");
			for (int i = 1; i < parts.length - 1; i++) {
				Matcher m = pattern.matcher(parts[i]);
				if (m.find()) header.setParameters(m.group(1).split(","));
				else mods.add(parts[i]);
			}
			String[] out = new String[mods.size()];
			out = mods.toArray(out);
			header.setModifiers(out);
		}
		return header;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String[] getModifiers() {
		return modifiers;
	}
	
	public void setModifiers(String[] modifiers) {
		this.modifiers = modifiers;
	}
	
	public String[] getParameters() {
		return parameters;
	}
	
	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}
	
	public Block toBlock(Shadow shadow) {
		Block block = new Block(shadow, getName(), line);
		block.setModifiers(getModifiers());
		block.setParameters(getParameters());
		return block;
	}
	
	public boolean isKeywordBlock() {
		return parameters.length == 0;
	}
}
