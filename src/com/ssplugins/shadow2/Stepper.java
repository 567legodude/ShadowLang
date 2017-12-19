package com.ssplugins.shadow2;

import com.ssplugins.shadow2.element.ShadowElement;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Stepper {
	
	private Scope scope;
	
	private ListIterator<ShadowElement> iterator;
	private StepperAction stepperAction;
	private Runnable onFinish;
	private StepAction action = StepAction.NORMAL;
	
	public Stepper(List<ShadowElement> list, Scope scope) {
		this.iterator = Collections.unmodifiableList(list).listIterator();
		this.scope = scope;
	}
	
	private void resetIterator() {
		while (iterator.previousIndex() > 0) {
			iterator.previous();
		}
	}
	
	private void onStep(ShadowElement element) {
		if (stepperAction != null) stepperAction.onAction(this, scope, element);
	}
	
	private void onFinish() {
		if (onFinish != null) onFinish.run();
		if (action == StepAction.RESTART) {
			resetIterator();
			start();
		}
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
			if (action == StepAction.NORMAL) {
				onStep(iterator.next());
			}
			else if (action == StepAction.RESTART) {
				resetIterator();
				action = StepAction.NORMAL;
			}
			else if (action == StepAction.PAUSE) break;
			else if (action == StepAction.BREAK) break;
			else if (action == StepAction.BREAK_ALL) break;
		}
		if (action != StepAction.PAUSE) {
			if (action != StepAction.BREAK_ALL) onFinish();
		}
	}
	
	public enum StepAction {
		NORMAL,
		RESTART,
		PAUSE,
		BREAK,
		BREAK_ALL
	}
	
}
