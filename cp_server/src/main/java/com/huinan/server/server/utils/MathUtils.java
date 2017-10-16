package com.huinan.server.server.utils;


import java.util.Random;

/**
 *
 * ashley
 */
public class MathUtils {
    private MathUtils(){}
    /**
     * get an int random number X, min <= X <= max
     *  
     * @param max
     * @param min
     * @return
     */
    public static int getRandom(int min, int max) {
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }
}
