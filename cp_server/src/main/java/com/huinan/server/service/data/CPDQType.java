package com.huinan.server.service.data;

public enum CPDQType {
	GY(1), NC(2);
	
	private final int value;

    private CPDQType(int value) {
		this.value =value;
	}

	public int getValue() {
		return value;
	}
	
}
