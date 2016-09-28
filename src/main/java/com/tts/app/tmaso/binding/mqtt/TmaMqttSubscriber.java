package com.tts.app.tmaso.binding.mqtt;

import java.util.Arrays;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.core.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.mqtt.msg.GetSetMessageBody;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessageBody;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.ChannelType;
import com.tts.app.tmaso.binding.type.MessageHelper;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public abstract class TmaMqttSubscriber implements MqttMessageConsumer {

    protected static Logger logger = LoggerFactory.getLogger(TmaMqttSubscriber.class);

    protected String topic;
    protected EventPublisher eventPublisher;

    public TmaMqttSubscriber(String topic) {
        this.topic = topic;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        if (isMatched(topic)) {
            String data = new String(payload);
            MqttMessage mqttMessage = parse(data);
            processMessage(mqttMessage);
        }
    }

    protected MqttMessage parse(String data) {
        /**
         * DATA FORMAT: <ACTION>;<DEVICE UID>;<..............>
         * ------------ |-------------------| |--------------|
         * -------------------- header ------------ body
         *
         * <ACTION>: register, ping, pong, get, set
         * <DEVICE UID>: unique value of the device
         *
         * REGISTER MSG:
         * ------- register;uid;<Friendly Name>;<Device Type>;<IP Address>;<Device
         * Path>;<channelName1:channelType1#channelName2:channelType2>
         *
         * ------------- <Device Type>: alarm, door, lcd, light, statussensor, valuesensor
         * ------------- <Device Path>:
         * ---------------------- Handle command topic: <Device Path>
         * ---------------------- Broadcast state topic: <Device Path>/broadcast
         * ------------- <channelName1:channelType1#channelName2:channelType2>:
         * ---------------------- This is channels meta data pair: <channel name>:<channel type>
         * ---------------------- <channel name>: string value
         * ---------------------- <channel type>: status (ON/OFF value), number, string
         */
        String[] arr = data.split(MqttConstants.SEPARATOR);

        MqttMessage rs = new MqttMessage();
        // MQTT Action
        rs.setMqttAction(MqttAction.fromString(arr[0].trim()));
        // MQTT UID
        rs.setUid(arr[1].trim());

        String[] bodyArr = Arrays.copyOfRange(arr, 2, arr.length);
        // MQTT Body
        MqttMessageBody msgBody = parseMessageBody(rs, bodyArr);
        rs.setBody(msgBody);
        return rs;
    }

    protected MqttMessageBody parseMessageBody(MqttMessage rs, String[] bodyArr) {
        return null;
    }

    protected MqttMessageBody parseSetGetMessage(ManagedDevice device, MqttMessage mqttMessage, String[] bodyArr) {
        // Body's format: <channelName>:<channelValue>#<channelName>:<channelValue>#<channelName>:<channelValue>
        GetSetMessageBody body = MqttMessageBody.getSetBody();

        String[] channelPairStrings = bodyArr[0].split(MqttConstants.SEPARATOR_CHANNEL);
        for (String channelPairString : channelPairStrings) {
            String[] arr = channelPairString.split("\\:");
            String channelName = arr.length == 1 ? MqttConstants.CHANNEL_NAME_DEFAULT : arr[0];
            String channelValue = arr.length == 1 ? arr[0] : arr[1];

            ChannelMetaData channel = device.getChannelMetaData(channelName);
            if (channel == null) {
                logger.error("Invalid channel name '{}' sent to device '{}'", channelName, device.getUid());
            } else {
                if (channel.getValue().equals(ChannelType.OnOff) || channel.getValue().equals(ChannelType.Status)) {
                    body.channel(channelName, MessageHelper.convertToESHOnOff(channelValue.trim()));
                } else if (channel.getValue().equals(ChannelType.OpenClosed)) {
                    body.channel(channelName, MessageHelper.convertToESHOpenClosed(channelValue.trim()));
                } else {
                    body.channel(channelName, new StringType(channelValue.trim()));
                }
            }

        }
        return body.cast();
    }

    protected abstract void processMessage(MqttMessage mqttMessage);

    private boolean isMatched(String topic) {
        return this.topic.equalsIgnoreCase(topic);
    }
}
