package com.tts.app.tmaso.binding.mqtt;

import org.openhab.core.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;

import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class DebugSubscriber implements MqttMessageConsumer {

    private TmaDeviceManager deviceManager;
    private String topic = MqttConstants.TOPIC_DEBUG;

    public DebugSubscriber(TmaDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        deviceManager.debug();
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {

    }

}
