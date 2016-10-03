package com.tts.app.tmaso.binding.mqtt;

import org.openhab.io.transport.mqtt.MqttMessageConsumer;

import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class SystemControlSubscriber extends TmaMqttSubscriber implements MqttMessageConsumer {

    private TmaDeviceManager deviceManager;

    public SystemControlSubscriber(TmaDeviceManager deviceManager) {
        super(MqttConstants.TOPIC_SYSTEM);
        this.deviceManager = deviceManager;
    }

    @Override
    protected void processMessageInThread(String topic, String data) {
        String[] params = data.trim().split(";");
        if (params.length == 0) {
            return;
        }

        if ("reset".equalsIgnoreCase(params[0].trim())) {
            doReset(params);
        } else if ("ping".equalsIgnoreCase(params[0].trim())) {
            deviceManager.ping();
        }
    }

    private void doReset(String[] params) {
        if (params.length > 1) {
            // reset specific device => 2nd is device's uid
            deviceManager.reset(params[1].trim());
        } else {
            // reset all
            deviceManager.reset(null);
        }
    }
}
