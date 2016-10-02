package com.tts.app.tmaso.binding.mqtt;

import java.util.Map;

import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.mqtt.msg.GetSetMessageBody;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessageBody;
import com.tts.app.tmaso.binding.mqtt.msg.RegisterMessageBody;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.ChannelType;
import com.tts.app.tmaso.binding.type.DeviceType;
import com.tts.app.tmaso.binding.type.MessageHelper;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class DiscoverySubscriber extends TmaMqttSubscriber {

    private TmaDeviceManager deviceManager;

    public DiscoverySubscriber(TmaDeviceManager deviceManager) {
        super(MqttConstants.TOPIC_DISCOVERY);
        this.deviceManager = deviceManager;
    }

    @Override
    protected MqttMessageBody parseMessageBody(MqttMessage mqttMessage, String[] bodyArr) {
        MqttAction action = mqttMessage.getAction();
        if (action.equals(MqttAction.REGISTER) || action.equals(MqttAction.PONG)) {
            return parseRegisterMessage(bodyArr);
        }
        if (action.equals(MqttAction.GET) || action.equals(MqttAction.GET_BULK) || action.equals(MqttAction.SET)
                || action.equals(MqttAction.SET_BULK)) {
            ManagedDevice device = deviceManager.getDevice(mqttMessage.getUid());
            return parseSetGetMessage(device, mqttMessage, bodyArr);
        }
        return super.parseMessageBody(mqttMessage, bodyArr);
    }

    @Override
    protected void processMessage(MqttMessage mqttMessage) {
        MqttAction action = mqttMessage.getAction();
        if (action.equals(MqttAction.REGISTER)) {
            processRegisterMessage(mqttMessage);
            return;
        }
        if (action.equals(MqttAction.PONG)) {
            processPongMessage(mqttMessage);
            return;
        }
        if (action.equals(MqttAction.GET) || action.equals(MqttAction.GET_BULK) || action.equals(MqttAction.SET)
                || action.equals(MqttAction.SET_BULK)) {
            processGetSetMessage(mqttMessage);
            return;
        }
    }

    private void processGetSetMessage(MqttMessage mqttMessage) {
        GetSetMessageBody body = mqttMessage.getBody();
        Map<String, State> attributes = body.channels();
        deviceManager.updateAttributes(mqttMessage.getUid(), attributes);
    }

    private MqttMessageBody parseRegisterMessage(String[] bodyArr) {
        // Sample: <Device Name>, <device type>, <192.168.1.1, 13>, </tma/lab2/tmp>, <channels>
        RegisterMessageBody rs = MqttMessageBody.registerBody();
        rs.friendlyName(bodyArr[0].trim());
        rs.deviceType(DeviceType.fromString(bodyArr[1].trim()));
        rs.ipAddress(bodyArr[2].trim());
        rs.devicePath(bodyArr[3].trim());
        if (bodyArr.length < 5) {
            rs.channel(ChannelMetaData.defaultPair());
        } else {
            String[] channelPairArr = bodyArr[4].trim().split(MqttConstants.SEPARATOR_CHANNEL);
            for (String channelPair : channelPairArr) {
                String[] arr = channelPair.split("\\:");
                rs.channel(new ChannelMetaData(arr[0].trim(), ChannelType.fromString(arr[1].trim())));
            }
        }
        return rs.cast();
    }

    private void processPongMessage(MqttMessage mqttMessage) {
        ManagedDevice device = MessageHelper.getDevice(mqttMessage);
        deviceManager.pong(device);
    }

    private void processRegisterMessage(MqttMessage mqttMessage) {
        ManagedDevice device = MessageHelper.getDevice(mqttMessage);
        deviceManager.register(device);
    }
}
