package com.ssplugins.shadow.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShadowUtil {
	
	static String[] getParts(String line) {
		List<String> f = new ArrayList<>();
		Matcher m = Pattern.compile("([^\\s]+?\\{.*?}\\S*|[^\"]\\S*|(?<!\\\\)\".+?(?<!\\\\)\"|\"\\S*)\\s*").matcher(line);
		while (m.find()) f.add(removeQuotes(m.group(1)));
		String[] out = new String[f.size()];
		out = f.toArray(out);
		return out;
	}
	
	static String removeQuotes(String s) {
		return s.replaceAll("(?<!\\\\)\"", "").replaceAll("\\\\\"", "\"");
	}
	
	static List<Section> createSections(Shadow shadow, List<String> lines, int line) {
		List<Section> sections = new ArrayList<>();
		int brackets = 0;
		int sLine = -1;
		List<String> currentSection = new ArrayList<>();
		for (String s : lines) {
			s = s.replace("\t", "").trim();
			if (s.isEmpty() || s.startsWith("//")) {
				line++;
				continue;
			}
			if (s.endsWith("{")) {
				brackets++;
				if (sLine == -1) sLine = line;
			}
			else if (s.equals("}")) {
				brackets--;
				if (brackets < 0) throw new ShadowParseException("Unexpected bracket on line " + line + ".");
			}
			currentSection.add(s);
			if (brackets == 0) {
				if (currentSection.size() == 1) {
					sections.add(new Section(new Line(shadow, currentSection.get(0), line)));
					currentSection.clear();
				}
				else {
					BlockHeader header = BlockHeader.parseHeader(new GenericLine(sLine, currentSection.get(0)));
					Block block = header.toBlock(shadow);
					block.addSections(createSections(shadow, currentSection.subList(1, currentSection.size() - 1), sLine + 1));
					if (block.getName().equalsIgnoreCase("box")) shadow.addBoxPattern(block);
					sections.add(new Section(block));
					currentSection.clear();
					sLine = -1;
				}
			}
			line++;
		}
		if (brackets != 0) throw new ShadowParseException("Bracket on line " + sLine + " is not closed.");
		return sections;
	}
	
	static Section toSection(Shadow shadow, String line) {
		return new Section(new Line(shadow, line, 1));
	}
	
	static Optional<Variable> getVariable(String var, Scope scope) {
		if (var.matches("p\\{.+}")) {
			return scope.getPrivateVar(var.substring(2, var.length() - 1));
		}
		else if (var.startsWith("g-")) {
			return scope.getGlobalVar(var.substring(2));
		}
		return scope.getVar(var);
	}
	
	static Optional<Variable> getGlobalVariable(String var, Scope scope) {
		if (var.startsWith("g-")) {
			return scope.getGlobalVar(var.substring(2));
		}
		return scope.getGlobalVar(var);
	}
	
	public static String combine(String[] parts, int start, int end) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < end; i++) {
			builder.append(parts[i]).append(" ");
		}
		return builder.toString().trim();
	}
	
	static String combine(Object parts) {
		if (!parts.getClass().isArray()) return "";
		int l = Array.getLength(parts);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < l; i++) {
			builder.append(Array.get(parts, i).toString()).append(" ");
		}
		return builder.toString().trim();
	}
	
	static String combine(Object[] parts, String del) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] == null) builder.append("null");
			else builder.append(parts[i].getClass().getName() + ":" + parts[i].toString()).append(del);
		}
		return builder.toString().substring(0, builder.length() - del.length());
	}
	
	static String combine(Object parts, int start) {
		if (!parts.getClass().isArray()) return "";
		int l = Array.getLength(parts);
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < l; i++) {
			builder.append(Array.get(parts, i).toString()).append(" ");
		}
		return builder.toString().trim();
	}
	
	static String combine(String[] parts, int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < parts.length; i++) {
			builder.append(parts[i]).append(" ");
		}
		return builder.toString().trim();
	}
	
	static Object[] toVarArgs(Object[] params, int index, Class<?> baseType) {
		int length = params.length - index;
		Object arr = Array.newInstance(baseType, length);
		for (int i = 0; i < length; i++) {
			Array.set(arr, i, params[i + index]);
		}
		params[index] = arr;
		return params;
	}
	
	static Object[] trim(Object[] params, int length) {
		return Arrays.copyOf(params, length);
	}
	
	static String getFromArray(Object array, int index) {
		if (!array.getClass().isArray()) return "";
		try {
			return String[].class.cast(array)[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	public static Optional<Variable> getLeveledVar(String var, Scope scope) {
		Matcher m = Pattern.compile("(\\^*)(.+)").matcher(var);
		if (m.find()) {
			String arrows = m.group(1);
			int level = arrows.isEmpty() ? 1 : arrows.length();
			String v = m.group(2);
			while (level > 0) {
				if (scope.levelUp() == null) {
					break;
				}
				scope = scope.levelUp();
				level--;
			}
			return scope.getVar(v);
		}
		return Optional.empty();
	}
	
	public static void pushLeveledVar(String var, Scope scope) {
		Matcher m = Pattern.compile("(\\^*)(.+)").matcher(var);
		if (m.find()) {
			String arrows = m.group(1);
			int level = arrows.isEmpty() ? 1 : arrows.length();
			String v = m.group(2);
			Optional<Variable> op = scope.getVar(v);
			if (!op.isPresent()) return;
			while (level > 0) {
				if (scope.levelUp() == null) {
					break;
				}
				scope = scope.levelUp();
				level--;
			}
			scope.add(op.get());
		}
	}
	
	public static String literal(String s) {
		if (s.substring(0, 1).matches("\\W")) return s;
		return "-" + s;
	}
}
