package com.tts.app.tmaso.binding.type;

public enum DeviceStatus {
    ONLINE,
    OFFLINE;

    public static DeviceStatus fromString(String val) {
        if (val.equalsIgnoreCase("online")) {
            return ONLINE;
        }
        return OFFLINE;
    }
}
