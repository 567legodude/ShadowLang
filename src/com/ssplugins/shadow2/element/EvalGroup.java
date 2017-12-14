package com.ssplugins.shadow2.element;

import com.ssplugins.shadow2.ShadowTools;

import java.util.List;

public class EvalGroup extends ShadowSection {
	
	private List<EvalSection> sections;
	
	public EvalGroup(List<EvalSection> sections) {
		this.sections = ShadowTools.lockList(sections);
	}
	
	public List<EvalSection> getSections() {
		return sections;
	}
	
	@Override
	public String toString() {
		return ShadowTools.asString(sections);
	}
	
}
