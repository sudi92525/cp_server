
package com.huinan.server.server.db;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * renchao
 */
public class RedisKeyManager {
    private static Map<String, String> keyMap = new HashMap<>();

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("key");
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = bundle.getString(key);
            keyMap.put((String) key, value);
        }
    }
    private RedisKeyManager() {
    }

    public static String getKey(String keyName) {
        return keyMap.get(keyName);
    }
    
}
