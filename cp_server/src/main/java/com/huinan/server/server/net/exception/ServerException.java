package com.huinan.server.server.net.exception;

public abstract class ServerException extends Exception {

    private static final long serialVersionUID = -6706071710906735532L;

    // errorCode must be a enum, for international / localization
    protected static int errorCode;

    public ServerException(String msg) {
        super(msg);
    }

    public int getErrorCode() {
        return errorCode;
    }

}
