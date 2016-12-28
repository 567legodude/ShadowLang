package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Block extends ShadowComponent {
	
	private String name;
	private List<String> modifiers = new ArrayList<>();
	private List<String> parameters = new ArrayList<>();
	private List<Section> sections = new ArrayList<>();
	
	private BlockPreRunEvent preRunEvent;
	private BlockEnterEvent enterEvent;
	private BlockEndEvent endEvent;
	
	Block(Shadow shadow, String name) {
		super(shadow);
		this.name = name;
	}
	
	void setModifiers(String[] mods) {
		modifiers.clear();
		for (String m : mods) modifiers.add(m);
	}
	
	void setParameters(String[] params) {
		parameters.clear();
		if (params == null) return;
		for (String p : params) parameters.add(p);
	}
	
	void addSection(Section section) {
		sections.add(section);
	}
	
	void addSections(List<Section> sections) {
		sections.forEach(this::addSection);
	}
	
	List<Section> getSections() {
		return sections;
	}
	
	public void findAllBlocks(String type, List<Block> blocks) {
		sections.stream().filter(Section::isBlock).map(Section::getBlock).filter(block -> block.getName().equalsIgnoreCase(type)).forEach(blocks::add);
		sections.stream().filter(Section::isBlock).map(Section::getBlock).forEach(block -> block.findAllBlocks(type, blocks));
	}
	
	public boolean verify(int mods, int params) {
		return modLength() == mods && paramLength() == params;
	}
	
	public void run(Object... params) {
		getShadow().runBlock(this, params);
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getModifiers() {
		return new ArrayList<>(modifiers);
	}
	
	public List<String> getParameters() {
		return new ArrayList<>(parameters);
	}
	
	public int modLength() {
		return modifiers.size();
	}
	
	public int paramLength() {
		return parameters.size();
	}
	
	public String getMod(int index) {
		if (index < 0 || index >= modifiers.size()) return null;
		return modifiers.get(index);
	}
	
	public String getParam(int index) {
		if (index < 0 || index >= parameters.size()) return null;
		return parameters.get(index);
	}
	
	public BlockPreRunEvent getPreRunEvent() {
		return preRunEvent;
	}
	
	public BlockEnterEvent getEnterEvent() {
		return enterEvent;
	}
	
	public BlockEndEvent getEndEvent() {
		return endEvent;
	}
	
	public void listen(BlockPreRunEvent event) {
		preRunEvent = event;
	}
	
	public void listen(BlockEnterEvent event) {
		enterEvent = event;
	}
	
	public void listen(BlockEndEvent event) {
		endEvent = event;
	}
}
