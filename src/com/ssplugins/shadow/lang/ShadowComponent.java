package com.ssplugins.shadow.lang;

public abstract class ShadowComponent {
	
	private Shadow shadow;
	
	ShadowComponent(Shadow shadow) {
		this.shadow = shadow;
	}
	
	public final Shadow getShadow() {
		return shadow;
	}
}
