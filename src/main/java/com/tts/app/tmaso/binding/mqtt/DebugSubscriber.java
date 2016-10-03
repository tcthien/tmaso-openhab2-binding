package com.tts.app.tmaso.binding.mqtt;

import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class DebugSubscriber extends TmaMqttSubscriber {

    private TmaDeviceManager deviceManager;

    public DebugSubscriber(TmaDeviceManager deviceManager) {
        super(MqttConstants.TOPIC_DEBUG);
        this.deviceManager = deviceManager;
    }

    @Override
    protected void processMessageInThread(String topic, String data) {
        deviceManager.debug();
    }
}
