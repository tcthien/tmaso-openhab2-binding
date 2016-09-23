package com.tts.app.tmaso.binding.type;

public class ChannelPair extends Entry<String, ChannelType> {
    public ChannelPair(String key, ChannelType obj) {
        super(key, obj);
    }

    public static ChannelPair defaultPair() {
        return new ChannelPair(MqttConstants.CHANNEL_NAME_DEFAULT, ChannelType.Status);
    }
}
