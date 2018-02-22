package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowElement;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Stepper {
	
	private Scope scope;
	private Stepper parent;
	
	private ListIterator<ShadowElement> iterator;
	private StepperAction stepperAction;
	private Runnable onFinish;
	private StepAction action = StepAction.NORMAL;
	
	private ShadowElement lastElement;
	private boolean lastElementRan;
	
	public Stepper(List<ShadowElement> list, Scope scope, Stepper parent) {
		this.iterator = Collections.unmodifiableList(list).listIterator();
		this.scope = scope;
		this.parent = parent;
	}
	
	private void resetIterator() {
		while (iterator.previousIndex() >= 0) {
			iterator.previous();
		}
	}
	
	private void onStep(ShadowElement element) {
		if (action == StepAction.BREAK_BLOCK_CHAIN) {
			if (element.isBlock()) return;
			else next(StepAction.NORMAL);
		}
		if (stepperAction != null) stepperAction.onAction(this, scope, element);
	}
	
	private void onFinish() {
		if (onFinish != null) onFinish.run();
		if (action == StepAction.RESTART) {
			resetIterator();
			start();
		}
	}
	
	private void breakAll() {
		next(StepAction.BREAK_ALL);
		if (parent != null) parent.breakAll();
	}
	
	public void setLastInfo(ShadowElement element, boolean ran) {
		this.lastElement = element;
		this.lastElementRan = ran;
	}
	
	public ShadowElement getLastElement() {
		return lastElement;
	}
	
	public boolean lastElementRan() {
		return lastElementRan;
	}
	
	public void setOnStep(StepperAction stepperAction) {
		this.stepperAction = stepperAction;
	}
	
	public void setOnFinish(Runnable onFinish) {
		this.onFinish = onFinish;
	}
	
	public void start() {
		action = StepAction.NORMAL;
		nextStep();
	}
	
	public void next(StepAction action) {
		this.action = (action == null ? StepAction.NORMAL : action);
	}
	
	public boolean willRestart() {
		return action == StepAction.RESTART;
	}
	
	private void nextStep() {
		if (!iterator.hasNext()) {
			onFinish();
			return;
		}
		while (iterator.hasNext()) {
			Debug.log("stepping: " + action.name());
			if (action == StepAction.NORMAL || action == StepAction.BREAK_BLOCK_CHAIN) {
				onStep(iterator.next());
			}
			else if (action == StepAction.RESTART) {
				resetIterator();
				scope.clearScope();
				action = StepAction.NORMAL;
			}
			else if (action == StepAction.PAUSE) break;
			else if (action == StepAction.BREAK) break;
			else if (action == StepAction.BREAK_ALL) {
				breakAll();
				break;
			}
		}
		if (action != StepAction.PAUSE) {
			onFinish();
		}
	}
	
	public enum StepAction {
		NORMAL,
		RESTART,
		PAUSE,
		BREAK,
		BREAK_ALL,
		BREAK_BLOCK_CHAIN
	}
	
}
