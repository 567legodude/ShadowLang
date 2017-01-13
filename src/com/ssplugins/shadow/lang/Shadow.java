package com.ssplugins.shadow.lang;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Shadow {
	
	private List<Section> sections = new ArrayList<>();
	private List<Variable> globalVars = new ArrayList<>();
	private List<Keyword> keywords = new ArrayList<>();
	private Map<String, Replacer> replacers = new HashMap<>();
	private Timer timer = new Timer();
	private Scope liveScope;
	
	private ClassFinder defaultFinder = this::defaultFinder;
	private ClassFinder finder = this::defaultFinder;
	
	private Shadow() {
		liveScope = new Scope(globalVars, null);
	}
	
	public static Shadow parse(List<String> lines) {
		Shadow shadow = new Shadow();
		if (lines == null || lines.size() == 0) return shadow;
		shadow.addSections(ShadowUtil.createSections(shadow, lines, 1));
		return shadow;
	}
	
	public static Shadow parse(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			return parse(reader.lines().collect(Collectors.toList()));
		} catch (FileNotFoundException e) {
			return new Shadow();
		}
	}
	
	public static Shadow empty() {
		return new Shadow();
	}
	
	Timer getTimer() {
		return timer;
	}
	
	List<Variable> getGlobalVars() {
		return globalVars;
	}
	
	List<Section> getSections() {
		return sections;
	}
	
	List<Section> getLinesAsSections() {
		return sections.stream().filter(Section::isLine).collect(Collectors.toList());
	}
	
	private String defaultFinder(String input) {
		boolean arr = input.endsWith("[]");
		if (arr) input = input.substring(0, input.length() - 2);
		for (Package p : Package.getPackages()) {
			try {
				Class<?> clazz = Class.forName(arr ? "[L" + p.getName() + "." + input + ";" : p.getName() + "." + input);
				return clazz.getName();
			} catch (ClassNotFoundException ignored) {
			}
		}
		return null;
	}
	
	public void end() {
		timer.cancel();
	}
	
	public Scope getLiveScope() {
		return liveScope;
	}
	
	private void addSections(List<Section> sections) {
		this.sections.addAll(sections);
	}
	
	public List<Block> getAllBlocks() {
		return sections.stream().filter(Section::isBlock).map(Section::getBlock).collect(Collectors.toList());
	}
	
	public List<Block> getBlocks(String type) {
		return sections.stream().filter(Section::isBlock).filter(section -> section.getBlock().getName().equalsIgnoreCase(type)).map(Section::getBlock).collect(Collectors.toList());
	}
	
	public List<Block> findAllBlocks(String type) {
		List<Block> blocks = new ArrayList<>();
		blocks.addAll(getBlocks(type));
		getAllBlocks().forEach(block -> block.findAllBlocks(type, blocks));
		return blocks;
	}
	
	public List<Line> getLines() {
		return sections.stream().filter(Section::isLine).map(Section::getLine).collect(Collectors.toList());
	}
	
	public boolean addKeyword(Keyword keyword) {
		if (keywordExists(keyword.getKeyword())) return false;
		keywords.add(keyword);
		return true;
	}
	
	public boolean keywordExists(String keyword) {
		return keywords.stream().anyMatch(keyword1 -> keyword1.getKeyword().equalsIgnoreCase(keyword));
	}
	
	public Optional<Keyword> getKeyword(String name) {
		return keywords.stream().filter(keyword -> keyword.getKeyword().equalsIgnoreCase(name)).findFirst();
	}
	
	public boolean renameKeyword(Keyword keyword, String name) {
		if (keywordExists(name)) return false;
		keyword.rename(name);
		return true;
	}
	
	public ClassFinder getClassFinder() {
		return finder;
	}
	
	public void setClassFinder(ClassFinder finder) {
		if (finder == null) return;
		this.finder = name -> {
			String n = finder.findClass(name);
			if (n != null) return n;
			return defaultFinder.findClass(name);
		};
	}
	
	public void setPreRunAction(String type, BlockPreRunEvent event) {
		findAllBlocks(type).forEach(block -> block.listen(event));
	}
	
	public void setEnterAction(String type, BlockEnterEvent event) {
		findAllBlocks(type).forEach(block -> block.listen(event));
	}
	
	public void setEndAction(String type, BlockEndEvent event) {
		findAllBlocks(type).forEach(block -> block.listen(event));
	}
	
	public boolean replacerExists(String pre) {
		return replacers.keySet().stream().anyMatch(s -> s.equalsIgnoreCase(pre));
	}
	
	public boolean addReplacer(String pre, Replacer replacer) {
		if (pre == null || pre.isEmpty() || replacer == null) return false;
		if (replacerExists(pre)) return false;
		replacers.put(pre, replacer);
		return true;
	}
	
	private void runReplacers(String[] args, Line line, Scope scope, Stepper stepper) {
		replacers.forEach((s, replacer) -> {
			String q = Pattern.quote(s);
			Pattern pattern = Pattern.compile(q + "\\{(.*?)}|" + q + "\\[(.*?)]");
			for (int i = 0; i < args.length; i++) {
				Matcher matcher = pattern.matcher(args[i]);
				while (matcher.find()) {
					String var = matcher.group(1);
					String replace = replacer.replace(var, line, scope, stepper);
					if (replace == null) continue;
					args[i] = args[i].replace(matcher.group(), replace);
				}
			}
		});
	}
	
	public void runBlocks(String type, Object... params) {
		getBlocks(type).forEach(block -> runBlock(block, params));
	}
	
	public void runBlock(Block block, Object... params) {
		if (block == null) {
			// run global lines
		}
		else {
			if (params.length != block.paramLength()) return;
			BlockPreRunEvent event = block.getPreRunEvent();
			boolean run = true;
			if (event != null) {
				run = event.trigger(block, null, null);
			}
			if (!run) return;
			Stepper stepper = new Stepper(block, null);
			for (int i = 0; i < params.length; i++) {
				stepper.setParam(i, params[i]);
			}
			stepper.start();
		}
	}
	
	public void parseLine(String line, MsgCallback callback, Variable... variables) {
		Stepper stepper = Stepper.prepare(this, line);
		for (Variable v : variables) stepper.inject(v);
		stepper.useMsgCallback(callback);
		stepper.start();
	}
	
	public void parseLine(String line) {
		parseLine(line, null);
	}
	
	void runLine(Line line, Scope scope, Stepper stepper) {
		Optional<Keyword> op = getKeyword(line.keyword());
		if (!op.isPresent()) {
			// either throw error or ignore
			return;
		}
		String[] args = line.getArguments();
		runReplacers(args, line, scope, stepper);
		op.get().execute(args, scope, stepper);
	}
	
}
