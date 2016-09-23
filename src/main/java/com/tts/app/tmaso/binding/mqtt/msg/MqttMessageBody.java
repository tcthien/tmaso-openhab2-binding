package com.tts.app.tmaso.binding.mqtt.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.type.ChannelPair;
import com.tts.app.tmaso.binding.type.DeviceType;

public class MqttMessageBody implements RegisterMessageBody, PingMessageBody, SetMessageBody {

    private static final String KEY_VALUE = "value";
    private static final String KEY_CHANNEL_NAME = "channelName";
    private static final String KEY_DEVICE_PATH = "devicePath";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_FRIENDLY_NAME = "friendlyName";

    private Map<String, Object> attributes = new HashMap<>();
    private List<ChannelPair> channels = new ArrayList<>();

    private MqttMessageBody() {
    }

    public static RegisterMessageBody registerBody() {
        return new MqttMessageBody();
    }

    public static PingMessageBody pingBody() {
        return new MqttMessageBody();
    }

    public static SetMessageBody setBody() {
        return new MqttMessageBody();
    }

    @Override
    public void friendlyName(String friendlyName) {
        attributes.put(KEY_FRIENDLY_NAME, friendlyName);
    }

    @Override
    public void deviceType(DeviceType deviceType) {
        attributes.put(KEY_DEVICE_TYPE, deviceType);
    }

    @Override
    public void ipAddress(String ipAddress) {
        attributes.put(KEY_IP_ADDRESS, ipAddress);
    }

    @Override
    public void devicePath(String devicePath) {
        attributes.put(KEY_DEVICE_PATH, devicePath);
    }

    @Override
    public void channel(ChannelPair channel) {
        channels.add(channel);
    }

    @Override
    public void channel(List<ChannelPair> channels) {
        channels.addAll(channels);
    }

    @Override
    public void channelName(String channelName) {
        attributes.put(KEY_CHANNEL_NAME, channelName);
    }

    @Override
    public void value(State value) {
        attributes.put(KEY_VALUE, value);
    }

    @Override
    public MqttMessageBody cast() {
        return this;
    }

    @Override
    public String friendlyName() {
        return (String) attributes.get(KEY_FRIENDLY_NAME);
    }

    @Override
    public String ipAddress() {
        return (String) attributes.get(KEY_IP_ADDRESS);
    }

    @Override
    public String devicePath() {
        return (String) attributes.get(KEY_DEVICE_PATH);
    }

    @Override
    public DeviceType deviceType() {
        return (DeviceType) attributes.get(KEY_DEVICE_TYPE);
    }

    @Override
    public List<ChannelPair> channels() {
        return channels;
    }

    @Override
    public String channelName() {
        return (String) attributes.get(KEY_CHANNEL_NAME);
    }

    @Override
    public State value() {
        return (State) attributes.get(KEY_VALUE);
    }
}
