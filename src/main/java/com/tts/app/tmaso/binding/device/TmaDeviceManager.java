package com.tts.app.tmaso.binding.device;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.type.DeviceStatus;
import com.tts.app.tmaso.binding.type.MqttAction;

public interface TmaDeviceManager {

    void register(ManagedDevice device);

    void pong(ManagedDevice device);

    DeviceStatus getStatus(String deviceUid);

    List<ManagedDevice> getAvailableDevices();

    ManagedDevice getDevice(String deviceUid);

    void updateAttributes(String uid, Map<String, State> attributes);

    void manageThingHandler(String deviceUid, TmaThingHandler thingHandler);

    void publishMqttCommand(ManagedDevice device, Command command, String channelId, MqttAction mqttAction)
            throws Exception;

    void debug();

}
