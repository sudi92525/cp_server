package com.huinan.server.service.data;

/**
 *
 * renchao
 */
public enum ERoomType {
    GY(1), NC(2);

    private final int value;

    private ERoomType(int value) {
	this.value = value;
    }

    public int getValue() {
	return value;
    }

}
