package com.huinan.server.net.clientfsm;

import com.huinan.server.net.clientfsm.clientstate.IClientFiniteState;

/**
 *
 * renchao
 */
public class FSTransition {
    private ClientStateTrans event;
    private IClientFiniteState originState;
    private IClientFiniteState targetState;

    public FSTransition(ClientStateTrans event, IClientFiniteState originState,
	    IClientFiniteState targetState) {
	this.event = event;
	this.originState = originState;
	this.targetState = targetState;
    }

    public ClientStateTrans getEvent() {
	return event;
    }

    public IClientFiniteState getOriginState() {
	return originState;
    }

    public IClientFiniteState getTargetState() {
	return targetState;
    }
}
