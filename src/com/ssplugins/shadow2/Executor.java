package com.ssplugins.shadow2;

import com.ssplugins.shadow2.Stepper.StepAction;
import com.ssplugins.shadow2.def.BlockDef;
import com.ssplugins.shadow2.def.KeywordDef;
import com.ssplugins.shadow2.element.Block;
import com.ssplugins.shadow2.element.Keyword;
import com.ssplugins.shadow2.element.ShadowElement;
import com.ssplugins.shadow2.exceptions.ShadowExecutionException;

import java.util.List;
import java.util.Optional;

public class Executor {
	
	private Scope scope;
	
	public Executor(Shadow shadow) {
		this.scope = new Scope(shadow.getContext());
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public void execute(Scope parentScope, Block block, Runnable onFinish, Object... params) {
		List<ShadowElement> list = block.getContent();
		Scope scope = parentScope.newChild();
		Stepper stepper = new Stepper(list, scope);
		Optional<BlockDef> op = scope.getContext().findBlock(block.getName());
		if (!op.isPresent()) {
			if (scope.getContext().getParseLevel().strictBlocks()) throw new ShadowExecutionException("Unknown block: " + block.getName(), block.getLine());
			return;
		}
		BlockDef def = op.get();
		boolean enter = ShadowTools.get(def.getEntryCondition()).map(condition -> condition.trigger(def, block.getModifiers(), scope, stepper)).orElse(true);
		if (!enter) return;
		if (params.length != block.getParameters().size()) throw new ShadowExecutionException("Block " + block.getName() + " expected " + block.getParameters().size() + " parameters, received " + params.length + ".");
		for (int i = 0; i < params.length; i++) {
			scope.setVar(block.getParameters().get(i), params[i]);
		}
		ShadowTools.get(def.getEnterEvent()).ifPresent(blockAction -> blockAction.trigger(def, block.getModifiers(), scope, stepper));
		stepper.setOnStep(this::run);
		stepper.setOnFinish(() -> {
			ShadowTools.get(def.getEndEvent()).ifPresent(blockAction -> blockAction.trigger(def, block.getModifiers(), scope, stepper));
			if (!stepper.willRestart() && onFinish != null) onFinish.run();
		});
	}
	
	public void execute(Block block, Object... params) {
		execute(scope, block, null, params);
	}
	
	private void run(Stepper stepper, Scope scope, ShadowElement element) {
		if (element.isBlock()) {
			stepper.next(StepAction.PAUSE);
			execute(scope, element.asBlock(), stepper::start);
		}
		else if (element.isKeyword()) {
			Keyword keyword = element.asKeyword();
			Optional<KeywordDef> op = scope.getContext().findKeyword(keyword.getKeyword());
			if (!op.isPresent()) {
				if (scope.getContext().getParseLevel().strictKeywords()) throw new ShadowExecutionException("Unknown keyword: " + keyword.getKeyword(), keyword.getLine());
				return;
			}
			KeywordDef def = op.get();
			ShadowTools.get(def.getAction()).ifPresent(keywordAction -> keywordAction.execute(def, keyword.getArguments(), scope, stepper));
		}
	}
	
}
