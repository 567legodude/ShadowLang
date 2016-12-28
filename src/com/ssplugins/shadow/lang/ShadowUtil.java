package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
	
	static String getFromArray(Object array, int index) {
		if (!array.getClass().isArray()) return "";
		try {
			return String[].class.cast(array)[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	static String getEvalParams(List<VariableType> list, Shadow shadow) {
		StringBuilder builder = new StringBuilder();
		Iterator<VariableType> it = list.iterator();
		while (it.hasNext()) {
			VariableType type = it.next();
			builder.append(shadow.getClassFinder().findClass(type.getType())).append(" ").append(type.getName());
			if (it.hasNext()) builder.append(", ");
		}
		return builder.toString();
	}
	
	static Class<?>[] getEvalClasses(List<VariableType> list, Shadow shadow) {
		List<Class<?>> out = new ArrayList<>();
		out.add(Scope.class);
		Iterator<VariableType> it = list.iterator();
		while (it.hasNext()) {
			VariableType type = it.next();
			try {
				out.add(Class.forName(shadow.getClassFinder().findClass(type.getType())));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return out.toArray(new Class[out.size()]);
	}
	
	static Object[] getEvalObjects(List<VariableType> list, Scope scope) {
		List<Object> l = new ArrayList<>();
		l.add(scope);
		list.forEach(variableType -> {
			Optional<Variable> op = scope.getVar(variableType.getName());
			if (!op.isPresent()) return;
			l.add(op.get().getValue());
		});
		return l.toArray(new Object[l.size()]);
	}
}
