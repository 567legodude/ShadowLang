package com.ssplugins.shadow.lang;

import java.util.Iterator;
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
		keyBring();
	}
	
	private void addBlocks() {
		blockRepeat();
		blockFor();
		blockLoop();
		blockIf();
		blockElse();
	}
	
	private void addReplacers() {
		replacerEval();
		replacerString();
	}
	
	private void keyLog() {
		shadow.addKeyword(new Keyword("log", (args, scope, stepper) -> {
			if (args.length > 0) {
				System.out.println(String.join(" ", args));
				scope.msg("Message logged");
			}
			else scope.error("No arguments");
		}));
	}
	
	private void keySet() {
		shadow.addKeyword(new Keyword("set", (args, scope, stepper) -> {
			if (args.length < 2) {
				scope.error("Less than 2 arguments");
				return;
			}
			String var = args[0];
			String value = ShadowUtil.combine(args, 1);
			Object finalVal;
			if (value.startsWith(">>")) {
				value = value.substring(2);
				finalVal = Evaluator.process(value, scope, stepper.getShadow().getClassFinder());
				if (finalVal == null) {
					scope.error("Resulting value is null");
					return;
				}
			}
			else finalVal = value;
			scope.setVar(var, finalVal);
			scope.msg("Variable " + var + " set");
		}));
	}
	
	private void keySetG() {
		shadow.addKeyword(new Keyword("setg", (args, scope, stepper) -> {
			if (args.length < 2) {
				scope.error("Less than 2 arguments");
				return;
			}
			String var = args[0];
			String value = ShadowUtil.combine(args, 1);
			Object finalVal;
			if (value.startsWith(">>")) {
				value = value.substring(2);
				finalVal = Evaluator.process(value, scope, stepper.getShadow().getClassFinder());
				if (finalVal == null) {
					scope.error("Resulting value is null");
					return;
				}
			}
			else finalVal = value;
			scope.setGlobalVar(var, finalVal);
			scope.msg("Global variable " + var + " set");
		}));
	}
	
	private void keyUnset() {
		shadow.addKeyword(new Keyword("unset", (args, scope, stepper) -> {
			if (args.length < 1) {
				scope.error("No arguments");
				return;
			}
			scope.unset(args[0]);
			scope.msg("Variable " + args[0] + " unset");
		}));
	}
	
	private void keyUnsetG() {
		shadow.addKeyword(new Keyword("unsetg", (args, scope, stepper) -> {
			if (args.length < 1) {
				scope.error("No arguments");
				return;
			}
			scope.unsetG(args[0]);
			scope.msg("Global variable " + args[0] + " unset");
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
			if (args.length < 2) {
				scope.error("Less than 2 arguments");
				return;
			}
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
			scope.msg("Asserts: " + final1.equals(final2));
		}));
	}
	
	private void keyCall() {
		shadow.addKeyword(new Keyword("call", (args, scope, stepper) -> {
			if (args.length < 1) {
				scope.error("Missing argument");
				return;
			}
			Evaluator.process(ShadowUtil.combine(args, 0), scope, stepper.getShadow().getClassFinder());
		}));
	}
	
	private void keyBring() {
		shadow.addKeyword(new Keyword("bring", (args, scope, stepper) -> {
			if (args.length < 1) return;
			Optional<Variable> op = ShadowUtil.getLeveledVar(args[0], scope);
			if (!op.isPresent()) return;
			scope.add(op.get());
		}));
	}
	
	private void blockRepeat() {
		shadow.setPreRunAction("repeat", (block, scope, info) -> {
			if (!block.verify(1, 1)) return false;
			if (block.getMod(0).replaceAll("[^0-9]", "").isEmpty()) return false;
			return true;
		});
		shadow.setEnterAction("repeat", (block, scope, stepper) -> {
			scope.setVar(block.getParam(0), 1);
		});
		shadow.setEndAction("repeat", (block, scope, stepper) -> {
			int c = Integer.valueOf(block.getMod(0));
			if (stepper.currentIteration() < c) {
				scope.setVar(block.getParam(0), stepper.currentIteration() + 1);
				stepper.stepRestart();
			}
		});
	}
	
	private void blockFor() {
		shadow.setPreRunAction("for", (block, scope, info) -> {
			if (!block.verify(3, 1)) return false;
			for (int i = 0; i < 3; i++) if (!block.getMod(i).matches("-?[0-9]+")) return false;
			int start = Integer.valueOf(block.getMod(0));
			int end = Integer.valueOf(block.getMod(1));
			int step = Integer.valueOf(block.getMod(2));
			if (start == end) return false;
			if (step < 0) return start > end;
			else return end > start;
		});
		shadow.setEnterAction("for", (block, scope, stepper) -> {
			scope.setVar(block.getParam(0), Integer.valueOf(block.getMod(0)));
		});
		shadow.setEndAction("for", (block, scope, stepper) -> {
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
	
	private void blockLoop() {
		shadow.setPreRunAction("loop", (block, scope, info) -> {
			if (!block.verify(1, 1)) return false;
			Object test = Evaluator.process(block.getMod(0), scope, block.getShadow().getClassFinder());
			return BlockIterator.isIterable(test);
		});
		shadow.setEnterAction("loop", (block, scope, stepper) -> {
			if (scope.levelUp() == null) {
				stepper.stepBreak();
				return;
			}
			Object o = Evaluator.process(block.getMod(0), scope.levelUp(), block.getShadow().getClassFinder());
			Iterator it = BlockIterator.getIterator(scope, o);
			if (it == null || !it.hasNext()) {
				stepper.stepBreak();
				BlockIterator.finish(scope);
				return;
			}
			scope.setVar(block.getParam(0), it.next());
		});
		shadow.setEndAction("loop", (block, scope, stepper) -> {
			Iterator it = BlockIterator.getIterator(scope, null);
			if (it == null) return;
			if (!it.hasNext()) {
				BlockIterator.finish(scope);
				return;
			}
			scope.setVar(block.getParam(0), it.next());
			stepper.stepRestart();
		});
	}
	
	private void blockIf() {
		shadow.setPreRunAction("if", (block, scope, info) -> {
			if (!block.verify(2, 0)) return false;
			String arg1 = block.getMod(0);
			String arg2 = block.getMod(1);
			Object final1;
			Object final2;
			if (arg1.matches("e\\{.+}")) final1 = Evaluator.process(arg1.substring(2, arg1.length() - 1), scope, block.getShadow().getClassFinder());
			else {
				Optional<Variable> op = ShadowUtil.getVariable(arg1, scope);
				final1 = op.orElse(Variable.temp(arg1)).getValue();
			}
			if (arg2.matches("e\\{.+}")) final2 = Evaluator.process(arg2.substring(2, arg2.length() - 1), scope, block.getShadow().getClassFinder());
			else {
				Optional<Variable> op = ShadowUtil.getVariable(arg2, scope);
				final2 = op.orElse(Variable.temp(arg2)).getValue();
			}
			return final1 == null ? final2 == null : final1.equals(final2);
		});
	}
	
	private void blockElse() {
		shadow.setPreRunAction("else", (block, scope, info) -> {
			if (!block.verify(0, 0)) return false;
			if (info == null) return false;
			if (!info.lastBlockIs("if")) return false;
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
					Optional<Variable> opv = ShadowUtil.getVariable(varName, scope);
					return opv.map(variable -> ShadowUtil.combine(variable.getValue())).orElse(null);
				}
				else if (d.endsWith("+")) {
					Optional<Variable> opv = ShadowUtil.getVariable(varName, scope);
					return opv.map(variable -> ShadowUtil.combine(variable.getValue(), Integer.valueOf(d.substring(0, d.length() - 1)))).orElse(null);
				}
				int digit = Integer.valueOf(d);
				Optional<Variable> opv = ShadowUtil.getVariable(varName, scope);
				return opv.map(variable -> ShadowUtil.getFromArray(variable.getValue(), digit)).orElse(null);
			}
			else {
				Optional<Variable> opv = ShadowUtil.getVariable(text, scope);
				return opv.map(variable -> variable.getValue().toString()).orElse(null);
			}
		});
	}
	
	private void replacerEval() {
		shadow.addReplacer("e", (text, line, scope, stepper) -> {
			Object value = Evaluator.process(text, scope, stepper.getShadow().getClassFinder());
			String key = scope.newPrivateVar(value);
			return "p{" + key + "}";
		});
	}
	
}
