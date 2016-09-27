package com.tts.app.tmaso.binding.type;

public class ChannelMetaData extends Entry<String, ChannelType> {
    public ChannelMetaData(String key, ChannelType obj) {
        super(key, obj);
    }

    public static ChannelMetaData defaultPair() {
        return new ChannelMetaData(MqttConstants.CHANNEL_NAME_DEFAULT, ChannelType.Status);
    }
}
