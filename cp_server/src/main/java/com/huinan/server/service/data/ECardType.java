package com.huinan.server.service.data;

public enum ECardType {
    TIANHU(5), DIHU(5), QLQD(5), QQD(4), LQD(4), QINGDUI(3), QIDUI(2), QYS(2), NORMAL(
	    1);

    private final int value;

    private ECardType(int value) {
	this.value = value;
    }

    public int getValue() {
	return value;
    }

}
