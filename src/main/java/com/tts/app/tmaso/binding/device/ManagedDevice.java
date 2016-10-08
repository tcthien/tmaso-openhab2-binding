package com.tts.app.tmaso.binding.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.TMAUtil;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.DeviceType;

public class ManagedDevice {
    private String uid;
    private String name;
    private String ipAddress;
    private String path;
    private DeviceType deviceType;
    private Map<String, ChannelMetaData> channelMetaData = new HashMap<>();
    private Map<String, State> channel = new HashMap<>();

    @Override
    public String toString() {
        return TMAUtil.toString("[", uid, name, path, deviceType, "]");
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setChannelMetaData(List<ChannelMetaData> channels) {
        for (ChannelMetaData pair : channels) {
            this.channelMetaData.put(pair.getKey(), pair);
        }
    }

    public Map<String, ChannelMetaData> getChannelMetaData() {
        return channelMetaData;
    }

    public ChannelMetaData getChannelMetaData(String channelName) {
        return channelMetaData.get(channelName);
    }

    public Map<String, State> getChannels() {
        return channel;
    }

    public void setChannels(Map<String, State> channel) {
        this.channel = channel;
    }

    public void setChannel(String channelName, State value) {
        channel.put(channelName, value);
    }
}
