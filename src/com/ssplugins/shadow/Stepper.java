package com.ssplugins.shadow;

import com.ssplugins.shadow.element.ShadowElement;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Stepper {
	
	private Scope scope;
	private Stepper parent;
	
	private ListIterator<ShadowElement> iterator;
	private StepperAction stepperAction;
	private Runnable onFinish;
	private StepAction action = StepAction.NORMAL;
	
	private AtomicReference<ShadowElement> lastElement;
	private AtomicBoolean lastElementRan;
	
	public Stepper(List<ShadowElement> list, Scope scope, Stepper parent) {
		this.iterator = Collections.unmodifiableList(list).listIterator();
		this.scope = scope;
		this.parent = parent;
        if (parent != null) {
            lastElement = parent.lastElement;
            lastElementRan = parent.lastElementRan;
        }
        else {
            lastElement = new AtomicReference<>();
            lastElementRan = new AtomicBoolean();
        }
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
	    lastElement.set(element);
	    lastElementRan.set(ran);
	}
	
	public ShadowElement getLastElement() {
		return lastElement.get();
	}
	
	public boolean lastElementRan() {
		return lastElementRan.get();
	}
    
    public boolean followsBlock(String block) {
	    ShadowElement se = getLastElement();
        return se != null && se.isBlock() && se.asBlock().getName().equalsIgnoreCase(block);
    }
    
    public boolean followsKeyword(String keyword) {
        ShadowElement se = getLastElement();
        return se != null && se.isKeyword() && se.asKeyword().getKeyword().equalsIgnoreCase(keyword);
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
	
	public StepAction getAction() {
	    return action;
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
