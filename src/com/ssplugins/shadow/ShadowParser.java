package com.ssplugins.shadow;

import com.ssplugins.shadow.LineReader.LineData;
import com.ssplugins.shadow.Shadow.ShadowBuilder;
import com.ssplugins.shadow.common.ParseLevel;
import com.ssplugins.shadow.def.*;
import com.ssplugins.shadow.element.Block;
import com.ssplugins.shadow.element.Keyword;
import com.ssplugins.shadow.element.ShadowElement;
import com.ssplugins.shadow.element.ShadowSection;
import com.ssplugins.shadow.exceptions.ShadowParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ShadowParser {
	
	private List<ShadowAPI> apis = new ArrayList<>();
	private ParseLevel level = ParseLevel.NORMAL;
	
	public ShadowParser() {
		this.addApi(new ShadowCommons());
	}
	
	public static ShadowParser empty() {
		ShadowParser parser = new ShadowParser();
		parser.apis.clear();
		return parser;
	}
	
	public void addApi(ShadowAPI api) {
		if (api != null) apis.add(api);
	}
	
	public void insertApiFirst(ShadowAPI api) {
		apis.add(0, api);
	}
	
	private Keyword parseKeyword(LineData data, ParseContext context) {
		String keyword = data.getName();
		Optional<KeywordDef> op = context.findKeyword(keyword);
		if (!op.isPresent()) {
			if (context.getParseLevel().strictKeywords()) {
				throw new ShadowParseException("Unknown keyword: " + keyword, context);
			}
		}
		KeywordDef def = op.orElse(KeywordDef.temporary(keyword));
		String[] args = ShadowTools.get(def.getSplitter()).map(splitter -> splitter.split(data.getArgs(), context)).orElse(data.getSplitArgs());
		if (def.getArgumentCount().outsideRange(args.length)) {
			throw new ShadowParseException("Keyword " + keyword + " expects " + def.getArgumentCount().toString() + " arguments, counted " + args.length + ".", context);
		}
		SectionParser parser = ShadowTools.get(def.getSectionParser()).orElse(SectionParser.standard());
		return new Keyword(context, keyword, parser.getSections(args, context));
	}
	
	private Block parseBlock(LineData data, List<ShadowElement> content, ParseContext context) {
		String name = data.getName();
		Optional<BlockDef> op = context.findBlock(name);
		if (!op.isPresent()) {
			if (context.getParseLevel().strictBlocks()) {
				throw new ShadowParseException("Unkown block: " + name, context);
			}
		}
		BlockDef def = op.orElse(BlockDef.temporary(name));
		String[] mods = ShadowTools.get(def.getSplitter()).map(splitter -> splitter.split(data.getMods(), context)).orElse(data.getSplitMods());
		String[] params = data.getParams();
		if (def.getParameterCount().outsideRange(params.length)) {
			throw new ShadowParseException("Block " + name + " expects " + def.getModifierCount().toString() + " parameters, counted " + params.length + ".", context);
		}
		SectionParser parser = ShadowTools.get(def.getSectionParser()).orElse(SectionParser.standard());
		List<ShadowSection> sections = parser.getSections(mods, context);
		if (def.getModifierCount().outsideRange(sections.size())) {
			throw new ShadowParseException("Block " + name + " expects " + def.getModifierCount().toString() + " modifiers, counted " + mods.length + ".", context);
		}
		return new Block(context, name, sections, Arrays.asList(params), content);
	}
	
	private List<ShadowElement> parseElements(List<String> content, ShadowContext context) {
		Debug.log("Parsing elements");
		if (context == null) context = new ShadowContext(apis, level);
		List<String> lines = new ArrayList<>(content);
		context.getLineParsers().forEach(parser -> lines.replaceAll(parser::parse));
		apis.forEach(api -> api.peekLines(lines));
		List<ShadowElement> elements = new ArrayList<>();
		LineReader reader = new LineReader(content);
		while (reader.hasNextLine()) {
			context.nextLine();
			LineData data = reader.readNextLine();
			context.setRaw(data.getRaw());
			Debug.log(context.toString());
			Debug.log(data.getType().name());
			if (data.isEmpty()) continue;
			if (data.isInvalid()) throw new ShadowParseException("Invalid syntax.", context);
			else if (data.isKeyword()) {
				elements.add(parseKeyword(data, context));
			}
			else if (data.isBlockHeader()) {
				List<String> blockLines = reader.readToEndBracket();
				elements.add(parseBlock(data, parseElements(blockLines, context), context));
				context.nextLine();
			}
			else if (data.isBlockClose()) {
				throw new ShadowParseException("Too many closing brackets.", context);
			}
		}
		return elements;
	}
	
	public Shadow parse(List<String> content) {
		ShadowContext context = new ShadowContext(apis, level);
		List<ShadowElement> elements = parseElements(content, context);
		return new ShadowBuilder().elements(elements).context(context).build();
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
	
	public void setParseLevel(ParseLevel level) {
		this.level = level;
	}
	
}
