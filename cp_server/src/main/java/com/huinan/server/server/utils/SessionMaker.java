package com.huinan.server.server.utils;


import java.util.UUID;

/**
 * This class is used to generate an unique string id for player on server side
 * 
 * TODO: This is the 1st version, should improve this later
 * 
 * 
 * @author ashley
 *
 */
public class SessionMaker {
    private SessionMaker(){}

    public static synchronized String generate() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
