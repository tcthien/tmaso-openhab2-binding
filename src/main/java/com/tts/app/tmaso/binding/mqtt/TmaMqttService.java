package com.tts.app.tmaso.binding.mqtt;

import org.openhab.io.transport.mqtt.MqttMessageConsumer;

public interface TmaMqttService {

    void register(MqttMessageConsumer discoverySubscriber);

    void unregister(MqttMessageConsumer discoverySubscriber);

    void register(TmaMqttProducer producer);

    void unregister(TmaMqttProducer producer);

}
