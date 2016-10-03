package com.tts.app.tmaso.binding.mqtt;

import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class DevicePollingProducer extends TmaMqttProducer {

    public DevicePollingProducer(TmaDeviceManager deviceManager) {
        super(MqttConstants.TOPIC_PING);
    }

    public void ping() throws Exception {
        publish(MqttConstants.MSG_PING);
    }
}
