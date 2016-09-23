package com.tts.app.tmaso.binding.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tts.app.tmaso.binding.type.ChannelPair;
import com.tts.app.tmaso.binding.type.DeviceType;

public class TmaDevice {
    private String uid;
    private String name;
    private String ipAddress;
    private String path;
    private DeviceType deviceType;
    private Map<String, ChannelPair> channels = new HashMap<>();

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

    public void setChannels(List<ChannelPair> channels) {
        for (ChannelPair pair : channels) {
            this.channels.put(pair.getKey(), pair);
        }
    }

    public Map<String, ChannelPair> getChannels() {
        return channels;
    }

    public ChannelPair getChannel(String channelName) {
        return channels.get(channelName);
    }
}
