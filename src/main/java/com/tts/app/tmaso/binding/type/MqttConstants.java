package com.tts.app.tmaso.binding.type;

public interface MqttConstants {
    public static final String SEPARATOR = ";";
    public static final String SEPARATOR_CHANNEL = "#";

    // Topic
    public static final String TOPIC_DISCOVERY = "/tmaso/meta/discovery";
    public static final String TOPIC_PING = "/tmaso/meta/ping";

    public static final String CHANNEL_NAME_DEFAULT = "default";

    // Message content constants
    public static String MSG_PING = "ping" + SEPARATOR + "tmaso-openhab";

    public static String BROKER_NAME = "tmaso";
}
