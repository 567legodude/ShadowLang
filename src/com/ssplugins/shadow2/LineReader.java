package com.ssplugins.shadow2;

import com.ssplugins.shadow2.exceptions.ShadowParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineReader {
	
	private List<String> list;
	private ListIterator<String> iterator;
	
	public LineReader(List<String> list) {
		this.list = list;
		this.iterator = list.listIterator();
	}
	
	public boolean hasNextLine() {
		return iterator.hasNext();
	}
	
	public LineData readNextLine() {
		if (!hasNextLine()) throw new NoSuchElementException("No more lines.");
		return new LineData(iterator.next());
	}
	
	public List<String> readToEndBracket() {
		int start = iterator.nextIndex();
		int bracket = 1;
		while (hasNextLine()) {
			LineType type = readNextLine().getType();
			if (type == LineType.BLOCK_HEADER) bracket++;
			else if (type == LineType.BLOCK_CLOSE) bracket--;
			if (bracket == 0) {
				break;
			}
		}
		if (bracket > 0) throw new ShadowParseException("No ending bracket found.", start);
		return list.subList(start, iterator.previousIndex());
	}
	
	public enum LineType {
		BLOCK_HEADER,
		BLOCK_CLOSE,
		KEYWORD,
		EMPTY,
		INVALID;
	}
	
	public class LineData {
		
		private final Pattern BLOCK_PATTERN = Pattern.compile("^(\\w+)(?: ((?:\\S+(?:\\{.*})? ??)+))?(?: \\(([^)]+)\\))? ?\\{$");
		private final Pattern KEYWORD_PATTERN = Pattern.compile("^(\\w+)(?: ((?:\\S+(?:\\{.*})? ?)+))?$");
		private final Pattern ARG_SPLITTER = Pattern.compile("(\\w+)(?:\\{(.*)})? ?");
		
		private String raw;
		private LineType type;
		
		private String name;
		private String args;
		private String[] splitArgs;
		private String mods;
		private String[] splitMods;
		private String[] params;
		
		private LineData(String raw) {
			raw = raw.replace("\t", "").trim();
			this.raw = raw;
			if (raw.isEmpty()) {
				type = LineType.EMPTY;
				return;
			}
			else if (raw.equals("}")) {
				type = LineType.BLOCK_CLOSE;
				return;
			}
			boolean success;
			success = testLine(BLOCK_PATTERN, matcher -> {
				type = LineType.BLOCK_HEADER;
				name = matcher.group(1);
				String mods = matcher.group(2);
				if (mods != null) {
					LineData.this.mods = mods;
					LineData.this.splitMods = split(mods, ARG_SPLITTER);
				}
				else {
					LineData.this.mods = "";
					LineData.this.splitMods = new String[0];
				}
				String params = matcher.group(3);
				if (params != null) {
					LineData.this.params = params.split(", *");
				}
				else {
					LineData.this.params = new String[0];
				}
			});
			if (!success) {
				success = testLine(KEYWORD_PATTERN, matcher -> {
					type = LineType.KEYWORD;
					name = matcher.group(1);
					String args = matcher.group(2);
					if (args != null) {
						LineData.this.args = args;
						LineData.this.splitArgs = split(args, ARG_SPLITTER);
					}
					else {
						LineData.this.args = "";
						LineData.this.splitArgs = new String[0];
					}
				});
			}
			if (!success) type = LineType.INVALID;
		}
		
		private boolean testLine(Pattern pattern, Consumer<Matcher> consumer) {
			Matcher m = pattern.matcher(raw);
			if (m.find()) {
				consumer.accept(m);
				return true;
			}
			return false;
		}
		
		private String[] split(String input, Pattern pattern) {
			List<String> out = new ArrayList<>();
			Matcher m = pattern.matcher(input);
			while (m.find()) {
				out.add(m.group().trim());
			}
			return out.toArray(new String[out.size()]);
		}
		
		public LineType getType() {
			return type;
		}
		
		public boolean isBlockHeader() {
			return type == LineType.BLOCK_HEADER;
		}
		
		public boolean isBlockClose() {
			return type == LineType.BLOCK_CLOSE;
		}
		
		public boolean isKeyword() {
			return type == LineType.KEYWORD;
		}
		
		public boolean isEmpty() {
			return type == LineType.EMPTY;
		}
		
		public boolean isInvalid() {
			return type == LineType.INVALID;
		}
		
		public String getRaw() {
			return raw;
		}
		
		public String getName() {
			return name;
		}
		
		public String getArgs() {
			return args;
		}
		
		public String[] getSplitArgs() {
			return splitArgs;
		}
		
		public String getMods() {
			return mods;
		}
		
		public String[] getSplitMods() {
			return splitMods;
		}
		
		public String[] getParams() {
			return params;
		}
	}
	
}
