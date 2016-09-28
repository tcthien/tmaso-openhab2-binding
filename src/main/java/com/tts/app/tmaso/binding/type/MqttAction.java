package com.tts.app.tmaso.binding.type;

public enum MqttAction {
    NONE,
    REGISTER,
    PING,
    PONG,
    GET,
    GET_BULK,
    SET,
    SET_BULK;

    public static MqttAction fromString(String b) {
        switch (b) {
            case "reg":
                return REGISTER;
            case "ping":
                return PING;
            case "pong":
                return PONG;
            case "get":
                return GET;
            case "gets":
                return GET_BULK;
            case "set":
                return SET;
            case "sets":
                return SET_BULK;
        }
        return NONE;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
