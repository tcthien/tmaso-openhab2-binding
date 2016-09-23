package com.tts.app.tmaso.binding.mqtt;

import java.util.Arrays;

import org.openhab.core.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;

import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessageBody;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public abstract class TmaMqttSubscriber implements MqttMessageConsumer {

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

    protected abstract void processMessage(MqttMessage mqttMessage);

    private boolean isMatched(String topic) {
        return this.topic.equalsIgnoreCase(topic);
    }
}
