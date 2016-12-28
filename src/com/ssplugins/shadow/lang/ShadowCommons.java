package com.ssplugins.shadow.lang;

import java.util.Optional;

public class ShadowCommons {
	
	private Shadow shadow;
	
	private ShadowCommons(Shadow shadow) {
		this.shadow = shadow;
		addKeywords();
		addBlocks();
		addReplacers();
	}
	
	public static void apply(Shadow shadow) {
		new ShadowCommons(shadow);
	}
	
	private void addKeywords() {
		keyLog();
		keySet();
		keySetG();
		keyUnset();
		keyUnsetG();
		keyWait();
		keyBreak();
		keyBreakAll();
		keyAssert();
		keyCall();
	}
	
	private void addBlocks() {
		blockRepeat();
		blockFor();
		blockIf();
		blockElse();
	}
	
	private void addReplacers() {
		replacerString();
	}
	
	private void keyLog() {
		shadow.addKeyword(new Keyword("log", (args, scope, stepper) -> {
			if (args.length > 0) System.out.println(String.join(" ", args));
		}));
	}
	
	private void keySet() {
		shadow.addKeyword(new Keyword("set", (args, scope, stepper) -> {
			if (args.length < 2) return;
			String var = args[0];
			String value = ShadowUtil.combine(args, 1);
			Object finalVal;
			if (value.startsWith(">>")) {
				value = value.substring(2);
				finalVal = Evaluator.process(value, scope, stepper.getShadow().getClassFinder());
				if (finalVal == null) return;
			}
			else finalVal = value;
			scope.setVar(var, finalVal);
		}));
	}
	
	private void keySetG() {
		shadow.addKeyword(new Keyword("setg", (args, scope, stepper) -> {
			if (args.length < 2) return;
			String var = args[0];
			String value = ShadowUtil.combine(args, 1);
			Object finalVal;
			if (value.startsWith(">>")) {
				value = value.substring(2);
				finalVal = Evaluator.process(value, scope, stepper.getShadow().getClassFinder());
				if (finalVal == null) return;
			}
			else finalVal = value;
			scope.setGlobalVar(var, finalVal);
		}));
	}
	
	private void keyUnset() {
		shadow.addKeyword(new Keyword("unset", (args, scope, stepper) -> {
			if (args.length < 1) return;
			scope.unset(args[0]);
		}));
	}
	
	private void keyUnsetG() {
		shadow.addKeyword(new Keyword("unsetg", (args, scope, stepper) -> {
			if (args.length < 1) return;
			scope.unsetG(args[0]);
		}));
	}
	
	private void keyWait() {
		shadow.addKeyword(new Keyword("wait", (args, scope, stepper) -> {
			if (args.length < 1) return;
			long t = Long.parseLong(args[0]);
			stepper.stepDelay(t);
		}));
	}
	
	private void keyBreak() {
		shadow.addKeyword(new Keyword("break", (args, scope, stepper) -> {
			stepper.stepBreak();
		}));
	}
	
	private void keyBreakAll() {
		shadow.addKeyword(new Keyword("breakall", (args, scope, stepper) -> {
			stepper.breakAll();
		}));
	}
	
	private void keyAssert() {
		shadow.addKeyword(new Keyword("assert", (args, scope, stepper) -> {
			if (args.length < 2) return;
			String arg1 = args[0];
			String arg2 = args[1];
			Object final1;
			Object final2;
			if (arg1.matches("o\\{.+}")) final1 = Evaluator.process(arg1.substring(2, arg1.length() - 1), scope, stepper.getShadow().getClassFinder());
			else final1 = arg1;
			if (arg2.matches("o\\{.+}")) final2 = Evaluator.process(arg2.substring(2, arg2.length() - 1), scope, stepper.getShadow().getClassFinder());
			else final2 = arg2;
			if (!final1.equals(final2)) {
				stepper.stepBreak();
			}
		}));
	}
	
	private void keyCall() {
		shadow.addKeyword(new Keyword("call", (args, scope, stepper) -> {
			if (args.length < 1) return;
			Evaluator.process(ShadowUtil.combine(args, 0), scope, stepper.getShadow().getClassFinder());
		}));
	}
	
	private void blockRepeat() {
		shadow.setGlobalBlockAction("repeat", (block, scope, info) -> {
			if (!block.verify(1, 1)) return false;
			if (block.getMod(0).replaceAll("[^0-9]", "").isEmpty()) return false;
			return true;
		});
		shadow.setGlobalBlockAction("repeat", (block, scope) -> {
			scope.setVar(block.getParam(0), 1);
		});
		shadow.setGlobalBlockAction("repeat", (block, scope, stepper) -> {
			int c = Integer.valueOf(block.getMod(0));
			if (stepper.currentIteration() < c) {
				scope.setVar(block.getParam(0), stepper.currentIteration() + 1);
				stepper.stepRestart();
			}
		});
	}
	
	private void blockFor() {
		shadow.setGlobalBlockAction("for", (block, scope, info) -> {
			if (!block.verify(3, 1)) return false;
			for (int i = 0; i < 3; i++) if (!block.getMod(i).matches("-?[0-9]+")) return false;
			int start = Integer.valueOf(block.getMod(0));
			int end = Integer.valueOf(block.getMod(1));
			int step = Integer.valueOf(block.getMod(2));
			if (start == end) return false;
			if (step < 0) return start > end;
			else return end > start;
		});
		shadow.setGlobalBlockAction("for", (block, scope) -> {
			scope.setVar(block.getParam(0), Integer.valueOf(block.getMod(0)));
		});
		shadow.setGlobalBlockAction("for", (block, scope, stepper) -> {
			int start = Integer.valueOf(block.getMod(0));
			int end = Integer.valueOf(block.getMod(1));
			int step = Integer.valueOf(block.getMod(2));
			int next = start + (step * stepper.currentIteration());
			if ((step < 0 && next >= end) || (step >= 0 && next <= end)) {
				scope.setVar(block.getParam(0), next);
				stepper.stepRestart();
			}
		});
	}
	
	private void blockIf() {
		shadow.setGlobalBlockAction("if", (block, scope, info) -> {
			if (!block.verify(2, 0)) return false;
			String arg1 = block.getMod(0);
			String arg2 = block.getMod(1);
			Object final1;
			Object final2;
			if (arg1.matches("o\\{.+}")) final1 = Evaluator.process(arg1.substring(2, arg1.length() - 1), scope, block.getShadow().getClassFinder());
			else final1 = arg1;
			if (arg2.matches("o\\{.+}")) final2 = Evaluator.process(arg2.substring(2, arg2.length() - 1), scope, block.getShadow().getClassFinder());
			else final2 = arg2;
			return final1.equals(final2);
		});
	}
	
	private void blockElse() {
		shadow.setGlobalBlockAction("else", (block, scope, info) -> {
			if (!block.verify(0, 0)) return false;
			if (info == null) return false;
			Block pBlock = info.getPrevBlock();
			if (!pBlock.getName().equalsIgnoreCase("if")) return false;
			if (info.lastBlockRan()) return false;
			return true;
		});
	}
	
	private void replacerString() {
		shadow.addReplacer("$", (text, line, scope, stepper) -> {
			if (text.matches(".+?\\[[0-9+]*?]")) {
				String varName = text.substring(0, text.lastIndexOf('['));
				String d = text.substring(text.lastIndexOf('[') + 1, text.lastIndexOf(']'));
				if (d.isEmpty()) {
					Optional<Variable> opv = scope.getVar(varName);
					return opv.map(variable -> ShadowUtil.combine(variable.getValue())).orElse(null);
				}
				else if (d.endsWith("+")) {
					Optional<Variable> opv = scope.getVar(varName);
					return opv.map(variable -> ShadowUtil.combine(variable.getValue(), Integer.valueOf(d.substring(0, d.length() - 1)))).orElse(null);
				}
				int digit = Integer.valueOf(d);
				Optional<Variable> opv = scope.getVar(varName);
				return opv.map(variable -> ShadowUtil.getFromArray(variable.getValue(), digit)).orElse(null);
			}
			else {
				// TODO clean up
				Optional<Variable> opv = scope.getVar(text);
				if (opv.isPresent()) {
					Variable v = opv.get();
					return v.getValue().toString();
				}
				return null;
			}
		});
	}
	
}
