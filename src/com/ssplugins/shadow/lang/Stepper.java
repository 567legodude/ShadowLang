package com.ssplugins.shadow.lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

public class Stepper implements StepperInfo {
	
	private StepAction action = StepAction.NORMAL;
	private long actionVal = 0;
	private int iteration = 0;
	private boolean started = false;
	private Runnable callback;
	private Line currentLine;
	
	private Shadow shadow;
	private Scope scope;
	private List<Section> sections;
	private Iterator<Section> iterator;
	private Block block;
	private Stepper calling;
	
	private Block prevBlock = null;
	private Block running = null;
	private boolean prevRun = false;
	
	private Stepper(Shadow shadow, List<Section> sections) {
		this.shadow = shadow;
		scope = new Scope(shadow.getGlobalVars(), null, shadow);
		this.sections = sections;
	}
	
	Stepper(Block block, Stepper calling) {
		shadow = block.getShadow();
		scope = new Scope(shadow.getGlobalVars(), (calling != null ? calling.scope : null), shadow);
		this.sections = block.getSections();
		this.block = block;
		this.calling = calling;
	}
	
	public static Stepper prepare(Shadow shadow, String line) {
		List<Section> sections = new ArrayList<>();
		sections.add(ShadowUtil.toSection(shadow, line));
		return new Stepper(shadow, sections);
	}
	
	void inject(Variable variable) {
		scope.setVar(variable.getName(), variable.getValue());
	}
	
	Stepper getCalling() {
		return calling;
	}
	
	void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	void useMsgCallback(MsgCallback callback) {
		scope.setMsgCallback(callback);
	}
	
	public Block getBlock() {
		return block;
	}
	
	public Block getPrevBlock() {
		return prevBlock;
	}
	
	public boolean lastBlockRan() {
		return prevRun;
	}
	
	public boolean lastBlockIs(String type) {
		return prevBlock != null && prevBlock.getName().equalsIgnoreCase(type);
	}
	
	public BooleanValue is(String type) {
		return new BooleanValue(block != null && block.getName().equalsIgnoreCase(type));
	}
	
	public Shadow getShadow() {
		return shadow;
	}
	
	public String param(int index) {
		return getBlock().getParam(index);
	}
	
	public String firstParam() {
		return param(0);
	}
	
	public String mod(int index) {
		return getBlock().getMod(index);
	}
	
	public String firstMod() {
		return mod(0);
	}
	
	public void setParam(int index, Object value) {
		if (block == null) return;
		scope.setVar(block.getParameters().get(index), value);
	}
	
	public void normal() {
		action = StepAction.NORMAL;
		actionVal = 0;
	}
	
	public void stepDelay(long time) {
		if (time < 1) return;
		action = StepAction.DELAY;
		actionVal = time;
	}
	
	public void stepRestart() {
		action = StepAction.RESTART;
	}
	
	public void stepBreak() {
		action = StepAction.BREAK;
	}
	
	public void stepWait() {
		action = StepAction.WAIT;
	}
	
	public void breakAll() {
		if (calling != null) calling.breakAll();
		stepBreak();
	}
	
	public int currentIteration() {
		return iteration;
	}
	
	public Line currentLine() {
		return currentLine;
	}
	
	public void start() {
		if (!started) {
			started = true;
			resetIterator();
			if (block != null) {
				BlockEnterEvent event = block.getEnterEvent();
				if (event != null) event.trigger(block, scope, this);
			}
			if (action == StepAction.BREAK) {
				runCallback();
				return;
			}
			stepForward();
		}
	}
	
	private void resetIterator() {
		iterator = sections.iterator();
		iteration++;
	}
	
	private void runCallback() {
		if (block != null) {
			BlockEndEvent event = block.getEndEvent();
			normal();
			if (event != null) event.trigger(block, scope, this);
			if (action == StepAction.RESTART) {
				resetIterator();
				stepForward();
				return;
			}
		}
		if (callback != null) callback.run();
	}
	
	void stepForward() {
		if (!iterator.hasNext()) {
			runCallback();
			return;
		}
		boolean run = true;
		while (run && iterator.hasNext()) {
			prevBlock = running;
			runStep(iterator.next());
			if (action == StepAction.DELAY) {
				run = false;
				shadow.getTimer().schedule(new TimerTask() {
					@Override
					public void run() {
						stepForward();
					}
				}, actionVal);
			}
			else if (action == StepAction.RESTART) resetIterator();
			else if (action == StepAction.BREAK || action == StepAction.WAIT) break;
		}
		if (action == StepAction.BREAK || (!iterator.hasNext() && action != StepAction.DELAY)) runCallback();
	}
	
	private void runStep(Section section) {
		normal();
		if (section.isLine()) {
			prevRun = true;
			running = null;
			currentLine = section.getLine();
			section.getShadow().runLine(currentLine, scope, this);
		}
		else {
			Block block = section.getBlock();
			running = block;
			BlockPreRunEvent event = block.getPreRunEvent();
			boolean run = true;
			if (event != null) {
				run = event.trigger(block, scope, this);
			}
			if (!run) {
				prevRun = false;
				return;
			}
			prevRun = true;
			Stepper stepper = new Stepper(block, this);
			stepper.setCallback(this::stepForward);
			action = StepAction.WAIT;
			stepper.start();
		}
	}
	
}
