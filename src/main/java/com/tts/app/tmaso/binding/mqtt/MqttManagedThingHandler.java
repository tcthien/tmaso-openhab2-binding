package com.tts.app.tmaso.binding.mqtt;

import org.eclipse.smarthome.core.types.Command;

import com.tts.app.tmaso.binding.type.MqttAction;

public interface MqttManagedThingHandler {

    void initialize();

    void unitialize();

    void publish(Command command, String channelId, MqttAction mqttAction) throws Exception;
}
