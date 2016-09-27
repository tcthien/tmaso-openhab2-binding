package com.tts.app.tmaso.binding.mqtt.msg;

import java.util.List;

import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.DeviceType;

public interface RegisterMessageBody extends MessageBody {
    void friendlyName(String friendlyName);

    void deviceType(DeviceType deviceType);

    void ipAddress(String ipAddress);

    void devicePath(String devicePath);

    void channel(ChannelMetaData channel);

    void channel(List<ChannelMetaData> channels);

    String friendlyName();

    String ipAddress();

    String devicePath();

    DeviceType deviceType();

    List<ChannelMetaData> channelMetaData();
}
