package com.tts.app.tmaso.binding.mqtt;

import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.device.TmaDeviceManagerImpl;
import com.tts.app.tmaso.binding.mqtt.ThingSubscriber.ThingSubscriberCallback;
import com.tts.app.tmaso.binding.type.MqttAction;

public class MqttManagedThingHandlerImpl implements MqttManagedThingHandler {

    private static Logger logger = LoggerFactory.getLogger(MqttManagedThingHandlerImpl.class);

    private ManagedDevice device;
    private TmaDeviceManagerImpl deviceManager;

    ThingSubscriber thingSubscriber;
    ThingProducer thingProducer;

    private ThingSubscriberCallback thingCallback;

    private MqttManagedThingHandlerImpl(TmaDeviceManagerImpl deviceManager, ManagedDevice device,
            ThingSubscriberCallback thingCallback) {
        this.deviceManager = deviceManager;
        this.device = device;
        this.thingCallback = thingCallback;
    }

    public static MqttManagedThingHandlerImpl manage(TmaDeviceManagerImpl deviceManager, ManagedDevice device,
            ThingSubscriberCallback thingCallback) {
        return new MqttManagedThingHandlerImpl(deviceManager, device, thingCallback);
    }

    public ThingProducer getThingProducer() {
        return thingProducer;
    }

    public ThingSubscriber getThingSubscriber() {
        return thingSubscriber;
    }

    @Override
    public void publish(Command command, String channelId, MqttAction mqttAction) throws Exception {
        if (thingProducer != null) {
            thingProducer.publish(command, channelId, mqttAction);
        }
    }

    @Override
    public void initialize() {
        logger.info("Initialize resource for {}", device.getUid());
        thingSubscriber = new ThingSubscriber(thingCallback, device);
        deviceManager.getTmaMqttService().register(thingSubscriber);

        thingProducer = new ThingProducer(device);
        deviceManager.getTmaMqttService().register(thingProducer);
    }

    @Override
    public void unitialize() {
        logger.info("Uninitialize resource for {}", device.getUid());
        // Clean up subscriber
        deviceManager.getTmaMqttService().unregister(thingSubscriber);
        deviceManager.getTmaMqttService().unregister(thingProducer);
    }

}
