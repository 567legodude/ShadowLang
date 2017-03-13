package com.ssplugins.shadow.lang;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShadowUtil {
	
	static String[] getParts(String line) {
		List<String> f = new ArrayList<>();
		Matcher m = Pattern.compile("([^\"]\\S*|(?<!\\\\)\".+?(?<!\\\\)\")\\s*").matcher(line);
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
		String[] array = String[].class.cast(parts);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			builder.append(array[i]).append(" ");
		}
		return builder.toString().trim();
	}
	
	static String combine(Object[] parts, String del) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			builder.append(parts[i].getClass().getName() + ":" + parts[i].toString()).append(del);
		}
		return builder.toString().substring(0, builder.length() - del.length());
	}
	
	static String combine(Object parts, int start) {
		if (!parts.getClass().isArray()) return "";
		String[] array = String[].class.cast(parts);
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < array.length; i++) {
			builder.append(array[i]).append(" ");
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
		List<Object> vars = new ArrayList<>();
		for (int i = index; i < params.length; i++) {
			vars.add(baseType.cast(params[i]));
		}
		Object arr = Array.newInstance(baseType, vars.size());
		for (int i = 0; i < vars.size(); i++) {
			Array.set(arr, i, vars.get(i));
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
			int level = m.group(0).length();
			String v = m.group(1);
			while (level > 0) {
				if (scope.levelUp() == null) {
					level = 0;
					break;
				}
				scope = scope.levelUp();
				level--;
			}
			return scope.getVar(v);
		}
		return Optional.empty();
	}
}
