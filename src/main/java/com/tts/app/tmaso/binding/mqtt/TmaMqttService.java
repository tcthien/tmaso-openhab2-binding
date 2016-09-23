package com.tts.app.tmaso.binding.mqtt;

public interface TmaMqttService {

    void register(TmaMqttSubscriber discoverySubscriber);

    void unregister(TmaMqttSubscriber discoverySubscriber);

    void register(TmaMqttProducer producer);

    void unregister(TmaMqttProducer producer);

}
