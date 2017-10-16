
package com.huinan.server.service.data;

/**
 *
 * renchao
 */
public enum ERoomCardType {
    CREATOR(1),
    AA(2);
    
    private final int value;
    
    private ERoomCardType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
