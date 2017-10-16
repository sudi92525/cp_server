package com.huinan.server.net.clientfsm;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huinan.server.net.clientfsm.clientstate.DeadClientState;
import com.huinan.server.net.clientfsm.clientstate.IClientFiniteState;
import com.huinan.server.net.clientfsm.clientstate.NewClientState;
import com.huinan.server.net.clientfsm.clientstate.NormalClientState;
import com.huinan.server.net.handler.GameSvrHandler;

/**
 *
 * renchao
 */
public class ClientFSM {
    protected static final Logger LOGGER = LogManager
	    .getLogger(ClientFSM.class);

    private GameSvrHandler context;
    private List<IClientFiniteState> states = new ArrayList<>();
    private List<FSTransition> transitions = new ArrayList<>();

    private IClientFiniteState curState;

    public ClientFSM(GameSvrHandler context) {
	this.context = context;
	initFSM();
    }

    private void initFSM() {
	NewClientState newState = new NewClientState();
	DeadClientState deadState = new DeadClientState();
	NormalClientState normalState = new NormalClientState();
	addState(newState);
	addState(deadState);
	addState(normalState);

	addTransition(new FSTransition(ClientStateTrans.NEW_2_NORMAL, newState,
		normalState));
	addTransition(new FSTransition(ClientStateTrans.FORCE_2_DEAD, newState,
		deadState));
	addTransition(new FSTransition(ClientStateTrans.FORCE_2_DEAD,
		normalState, deadState));

	// default state
	changeState(newState);
    }

    private void addState(IClientFiniteState state) {
	states.add(state);
    }

    private void addTransition(FSTransition transition) {
	transitions.add(transition);
    }

    private void changeState(IClientFiniteState nextState) {
	if (curState != null) {
	    curState.exit(context);
	}

	curState = nextState;
	curState.enter(context);
    }

    public void fireEvent(ClientStateTrans event) {
	for (FSTransition tans : transitions) {
	    if (tans.getEvent() == event && tans.getOriginState() == curState) {
		LOGGER.debug(context.getChannel().hashCode() + " uid:"
			+ context.getUid() + ":change state, curState:"
			+ curState.getClass().getSimpleName() + " event:"
			+ event);
		changeState(tans.getTargetState());
		return;
	    }
	}
	LOGGER.debug(context.getChannel().hashCode() + " event:" + event
		+ " trans error in fireEvent(), curState:"
		+ curState.getClass().getSimpleName());
    }

    public IClientFiniteState getCurState() {
	return curState;
    }
}
