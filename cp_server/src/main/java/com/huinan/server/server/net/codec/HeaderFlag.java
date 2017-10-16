package com.huinan.server.server.net.codec;

/**
 *
 * ashley
 */
public enum HeaderFlag {
    NORMAL(1), GS_CLOSE_AFTER_SEND(2);

    private final int value;

    private HeaderFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public boolean Compare(int i) {
        return value == i;
    }

    public static HeaderFlag getHeader(int id) {
        HeaderFlag[] As = HeaderFlag.values();
        for (int i = 0; i < As.length; i++) {
            if (As[i].Compare(id))
                return As[i];
        }
        return null;
    }
}