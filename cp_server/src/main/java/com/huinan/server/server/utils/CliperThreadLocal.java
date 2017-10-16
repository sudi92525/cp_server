package com.huinan.server.server.utils;


import javax.crypto.Cipher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CliperThreadLocal {
    private static final Logger LOGGER = LogManager
            .getLogger(CliperThreadLocal.class);
    private CliperThreadLocal(){}
    
    private static JokerThreadLocal<Cipher> connectionHolder = new JokerThreadLocal<Cipher>() {
        @Override
        protected Cipher create() {
            try {
                return Cipher.getInstance("RSA");
            } catch (Exception e) {
                LOGGER.error("CliperThreadLocal error:", e);
            }
            return null;
        }
    };

    public static Cipher getInstance() {
        return connectionHolder.get(true);
    }
}
