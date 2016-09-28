package com.tts.app.tmaso.binding.device;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.types.State;

import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.type.DeviceStatus;

public interface TmaDeviceManager {

    void register(ManagedDevice device);

    DeviceStatus getStatus(String deviceUid);

    List<ManagedDevice> getAvailableDevices();

    ManagedDevice getDevice(String deviceUid);

    void updateAttributes(String uid, Map<String, State> attributes);

    void addThingHandler(String deviceUid, TmaThingHandler thingHandler);
}
