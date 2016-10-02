package com.tts.app.tmaso.binding.mqtt;

import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.openhab.io.transport.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.type.MqttConstants;

public class TmaMqttServiceImpl implements TmaMqttService {

    private static Logger logger = LoggerFactory.getLogger(TmaMqttServiceImpl.class);

    private MqttService mqttService;

    /**
     * Setter for Declarative Services. Adds the MqttService instance.
     *
     * @param mqttService
     *            Service.
     */
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    /**
     * Unsetter for Declarative Services.
     *
     * @param mqttService
     *            Service to remove.
     */
    public void unsetMqttService(MqttService mqttService) {
        this.mqttService = null;
    }

    @Override
    public void register(MqttMessageConsumer subscriber) {
        logger.info("'{}' is registered.", subscriber.getClass());
        this.mqttService.registerMessageConsumer(MqttConstants.BROKER_NAME, subscriber);
    }

    @Override
    public void unregister(MqttMessageConsumer subscriber) {
        logger.info("'{}' is unregistered.", subscriber.getClass());
        this.mqttService.unregisterMessageConsumer(MqttConstants.BROKER_NAME, subscriber);
    }

    @Override
    public void register(TmaMqttProducer producer) {
        logger.info("'{}' is registered.", producer.getClass());
        this.mqttService.registerMessageProducer(MqttConstants.BROKER_NAME, producer);
    }

    @Override
    public void unregister(TmaMqttProducer producer) {
        logger.info("'{}' is unregistered.", producer.getClass());
        this.mqttService.unregisterMessageProducer(MqttConstants.BROKER_NAME, producer);
    }
}
