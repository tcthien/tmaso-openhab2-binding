package com.tts.app.tmaso.binding.type;

public enum MqttAction {
    NONE,
    REGISTER,
    PING,
    PONG,
    GET,
    SET;

    public static MqttAction fromString(String b) {
        switch (b) {
            case "register":
                return REGISTER;
            case "ping":
                return PING;
            case "pong":
                return PONG;
            case "get":
                return GET;
            case "set":
                return SET;
        }
        return NONE;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
