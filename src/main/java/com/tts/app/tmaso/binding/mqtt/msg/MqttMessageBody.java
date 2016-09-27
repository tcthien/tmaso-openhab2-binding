package com.tts.app.tmaso.binding.mqtt.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.DeviceType;

public class MqttMessageBody implements RegisterMessageBody, PingMessageBody, GetSetMessageBody {

    private static final String KEY_VALUE = "value";
    private static final String KEY_CHANNEL_NAME = "channelName";
    private static final String KEY_DEVICE_PATH = "devicePath";
    private static final String KEY_IP_ADDRESS = "ipAddress";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_FRIENDLY_NAME = "friendlyName";

    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, State> channelValues = new HashMap<>();
    private List<ChannelMetaData> channels = new ArrayList<>();

    private MqttMessageBody() {
    }

    public static RegisterMessageBody registerBody() {
        return new MqttMessageBody();
    }

    public static PingMessageBody pingBody() {
        return new MqttMessageBody();
    }

    public static GetSetMessageBody getSetBody() {
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
    public void channel(ChannelMetaData channel) {
        channels.add(channel);
    }

    @Override
    public void channel(List<ChannelMetaData> channels) {
        channels.addAll(channels);
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
    public List<ChannelMetaData> channelMetaData() {
        return channels;
    }

    @Override
    public void channel(String channelName, State channelValue) {
        channelValues.put(channelName, channelValue);
    }

    @Override
    public State channelValue(String channelName) {
        return channelValues.get(channelName);
    }

    @Override
    public Map<String, State> channels() {
        return channelValues;
    }

}
