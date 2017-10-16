package com.huinan.server.net.clientfsm;

/**
 *
 * renchao
 */
public enum ClientStateTrans {

    NEW_2_NORMAL(0), FORCE_2_DEAD(1), PVP_2_NORMAL(2), ;

    private int value;

    private ClientStateTrans(int value) {
	this.value = value;
    }

    public int getValue() {
	return value;
    }

}
