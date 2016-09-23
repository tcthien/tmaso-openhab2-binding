package com.tts.app.tmaso.binding.mqtt;

import org.openhab.io.transport.mqtt.MqttMessageProducer;
import org.openhab.io.transport.mqtt.MqttSenderChannel;

public class TmaMqttProducer implements MqttMessageProducer {

    private MqttSenderChannel channel;
    private String topic;

    public TmaMqttProducer(String topic) {
        this.topic = topic;
    }

    @Override
    public void setSenderChannel(MqttSenderChannel channel) {
        this.channel = channel;
    }

    public String getTopic() {
        return topic;
    }

    public void publish(String data) throws Exception {
        if (channel != null) {
            channel.publish(topic, data.getBytes());
        }
    }
}
