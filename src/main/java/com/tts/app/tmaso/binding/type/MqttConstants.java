package com.tts.app.tmaso.binding.type;

public interface MqttConstants {
    public static final String SEPARATOR = ";";
    public static final String SEPARATOR_CHANNEL = "#";

    // Topic
    public static final String TOPIC_SYSTEM = "/tma/system";
    public static final String TOPIC_DEBUG = "/tma/debug";
    public static final String TOPIC_DISCOVERY = "/tma/discover";
    public static final String TOPIC_PING = "/tma/ping";

    public static final String CHANNEL_NAME_DEFAULT = "default";

    // Message content constants
    public static String MSG_PING = "ping" + SEPARATOR + "tmaso-openhab";

    public static String BROKER_NAME = "tmaso";
}
