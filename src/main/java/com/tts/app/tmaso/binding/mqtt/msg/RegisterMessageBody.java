package com.tts.app.tmaso.binding.mqtt.msg;

import java.util.List;

import com.tts.app.tmaso.binding.type.ChannelPair;
import com.tts.app.tmaso.binding.type.DeviceType;

public interface RegisterMessageBody extends MessageBody {
    void friendlyName(String friendlyName);

    void deviceType(DeviceType deviceType);

    void ipAddress(String ipAddress);

    void devicePath(String devicePath);

    void channel(ChannelPair channel);

    void channel(List<ChannelPair> channels);

    String friendlyName();

    String ipAddress();

    String devicePath();

    DeviceType deviceType();

    List<ChannelPair> channels();
}
