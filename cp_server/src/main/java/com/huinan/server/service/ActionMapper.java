package com.huinan.server.service;

import com.huinan.proto.CpMsg.CpMsgData;
import com.huinan.server.service.action.Chat;
import com.huinan.server.service.action.GameAction;
import com.huinan.server.service.action.GetPlayBack;
import com.huinan.server.service.action.HeartBeat;
import com.huinan.server.service.action.Login;
import com.huinan.server.service.action.Online;
import com.huinan.server.service.action.ReConnect;
import com.huinan.server.service.action.Ready;
import com.huinan.server.service.action.RoomDissolve;
import com.huinan.server.service.action.RoomCreate;
import com.huinan.server.service.action.RoomJoin;
import com.huinan.server.service.action.RoomPleaseOut;
import com.huinan.server.service.action.RoomQuit;

/**
 *
 * renchao
 */
public class ActionMapper {

    public static AbsAction getActor(int systemId) {
	switch (systemId) {
	case CpMsgData.CS_REQUEST_LOGIN_FIELD_NUMBER:
	    return new Login();
	case CpMsgData.CS_REQUEST_CREATE_TABLE_FIELD_NUMBER:
	    return new RoomCreate();
	case CpMsgData.CS_REQUEST_ENTER_TABLE_FIELD_NUMBER:
	    return new RoomJoin();
	case CpMsgData.CS_REQUEST_LOGOUT_TABLE_FIELD_NUMBER:
	    return new RoomQuit();
	case CpMsgData.CS_REQUEST_READY_FOR_GAME_FIELD_NUMBER:
	    return new Ready();
	case CpMsgData.CS_REQUEST_DO_ACTION_FIELD_NUMBER:
	    return new GameAction();
	case CpMsgData.CS_REQUEST_DISSOLVE_TABLE_FIELD_NUMBER:
	    return new RoomDissolve();
	case CpMsgData.CS_REQUEST_HEART_BEAT_FIELD_NUMBER:
	    return new HeartBeat();
	case CpMsgData.CS_REQUEST_RECONNECT_FIELD_NUMBER:
	    return new ReConnect();
	case CpMsgData.CS_REQUEST_CHAT_FIELD_NUMBER:
	    return new Chat();
	case CpMsgData.CS_REQUEST_IS_ONLINE_FIELD_NUMBER:
	    return new Online();
	case CpMsgData.CS_REQUEST_OWNER_TIREN_FIELD_NUMBER:
	    return new RoomPleaseOut();
	case CpMsgData.CS_REQUEST_PALY_BACK_FIELD_NUMBER:
	    return new GetPlayBack();
	default:
	    return null;
	}
    }

    private ActionMapper() {
    }
}
