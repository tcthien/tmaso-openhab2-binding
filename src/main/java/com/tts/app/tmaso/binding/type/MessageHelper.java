package com.tts.app.tmaso.binding.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.RegisterMessageBody;

public class MessageHelper {
    public static String getChannelCommandTopic(String devicePath, String channelName) {
        return devicePath + "/" + channelName;
    }

    public static String getChannelBroadcastTopic(String devicePath, String channelName) {
        return devicePath + "/" + channelName + "/broadcast";
    }

    public static ManagedDevice getDevice(MqttMessage mqttMessage) {
        ManagedDevice rs = new ManagedDevice();
        rs.setUid(mqttMessage.getUid());
        if (mqttMessage.getBody() instanceof RegisterMessageBody) {
            RegisterMessageBody body = mqttMessage.getBody();
            rs.setName(body.friendlyName());
            rs.setIpAddress(body.ipAddress());
            rs.setPath(body.devicePath());
            rs.setDeviceType(body.deviceType());
            rs.setChannelMetaData(body.channelMetaData());
        }
        return rs;
    }

    public static Set<ThingTypeUID> asSet(ThingTypeUID... typeUIDs) {
        return new HashSet<>(Arrays.asList(typeUIDs));
    }

    public static OnOffType convertToESHOnOff(String b) {
        switch (b) {
            case "on":
                return OnOffType.ON;
            case "ON":
                return OnOffType.ON;
            case "off":
                return OnOffType.OFF;
            case "OFF":
                return OnOffType.OFF;
            default:
                return OnOffType.OFF;
        }
    }

}
