package com.huinan.server.server.utils;


/**
 *
 * ashley
 */
public class TimeUtils {
    private TimeUtils(){}
    /**
     * 
     * @param expire
     *            unit: minutes
     * @return
     */
    public static long getEndTimestamp(int expire) {
        long startTime = System.currentTimeMillis();
        return startTime + expire * 1000;
    }

}
