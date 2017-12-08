package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.List;

public class BoxPattern {

	private String name;
	private List<VariableType> constructor;
	
	private ClassFinder finder;
	
	BoxPattern(String name, ClassFinder finder) {
		this.name = name;
		constructor = new ArrayList<>();
		this.finder = finder;
	}
	
	public String getName() {
		return name;
	}
	
	public void read(Block block) {
		constructor.clear();
		block.getSections().stream().filter(Section::isLine).map(Section::getLine).forEach(line -> {
			if (line.keyword().equalsIgnoreCase("var")) {
				String[] args = line.getArguments();
				if (args.length < 2) return;
				String clazz = finder.findClass(args[0]);
				if (clazz == null) return;
				constructor.add(new VariableType(clazz, args[1]));
			}
		});
	}
	
	public Box create(List<Object> params) {
		Box box = new Box();
		if (params.size() == constructor.size()) {
			for (int i = 0; i < params.size(); i++) {
				VariableType type = constructor.get(i);
				Object param = params.get(i);
				try {
					box.newVar(type.getName(), Class.forName(type.getType()), param);
				} catch (ClassNotFoundException ignored) {
				}
			}
		}
		else constructor.forEach(variableType -> {
			try {
				box.newVar(variableType.getName(), Class.forName(variableType.getType()));
			} catch (ClassNotFoundException ignored) {
			}
		});
		return box;
	}
	
}
