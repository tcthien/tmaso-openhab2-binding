package com.tts.app.tmaso.binding.device;

import java.util.List;

import com.tts.app.tmaso.binding.type.DeviceStatus;

public interface TmaDeviceManager {

    void register(TmaDevice device);

    DeviceStatus getStatus(String deviceUid);

    List<TmaDevice> getAvailableDevices();

    TmaDevice getDevice(String deviceUid);
}
