package com.tts.app.tmaso.binding.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.ComponentActivator;
import com.tts.app.tmaso.binding.mqtt.DevicePollingProducer;
import com.tts.app.tmaso.binding.mqtt.DiscoverySubscriber;
import com.tts.app.tmaso.binding.mqtt.TmaMqttService;
import com.tts.app.tmaso.binding.type.DeviceStatus;

public class TmaDeviceManagerImpl implements TmaDeviceManager, ComponentActivator {

    private static Logger logger = LoggerFactory.getLogger(TmaDeviceManagerImpl.class);

    private TmaMqttService tmaMqttService;

    private DiscoverySubscriber discoverySubscriber;
    private DevicePollingProducer pollingProducer;

    private Map<String, ManagedDevice> devices = new ConcurrentHashMap<>();
    private Map<String, Date> deviceUpdatedTime = new ConcurrentHashMap<>();

    @Override
    public ManagedDevice getDevice(String deviceUid) {
        return devices.get(deviceUid);
    }

    @Override
    public List<ManagedDevice> getAvailableDevices() {
        List<ManagedDevice> rs = new ArrayList<>();
        for (ManagedDevice device : devices.values()) {
            if (getStatus(device.getUid()).equals(DeviceStatus.ONLINE)) {
                rs.add(device);
            }
        }
        return rs;
    }

    @Override
    public DeviceStatus getStatus(String deviceUid) {
        Date date = deviceUpdatedTime.get(deviceUid);
        return date == null || (pollingProducer.getPingTime() != null && date.before(pollingProducer.getPingTime()))
                ? DeviceStatus.OFFLINE : DeviceStatus.ONLINE;
    }

    @Override
    public void register(ManagedDevice device) {
        deviceUpdatedTime.put(device.getUid(), new Date());
        devices.put(device.getUid(), device);
    }

    @Override
    public void updateAttributes(String uid, Map<String, State> attributes) {
        ManagedDevice device = devices.get(uid);
        if (device != null) {
            for (Entry<String, State> entry : attributes.entrySet()) {
                device.setChannel(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void activate(ComponentContext context) {
        // Initialize Service
        logger.info("'{}' is initailizing...", getClass().getName());
        discoverySubscriber = new DiscoverySubscriber(this);
        pollingProducer = new DevicePollingProducer(this);
        pollingProducer.initialize();
        tmaMqttService.register(discoverySubscriber);
        tmaMqttService.register(pollingProducer);
    }

    @Override
    public void deactivate(ComponentContext context) {
        // Un-initialize Service
        logger.info("'{}' is uninitailizing...", getClass().getName());
        pollingProducer.uninitialize();
        tmaMqttService.unregister(discoverySubscriber);
        tmaMqttService.unregister(pollingProducer);
    }

    public void setTmaMqttService(TmaMqttService tmaMqttService) {
        this.tmaMqttService = tmaMqttService;
    }

    public void unSetTmaMqttService(TmaMqttService tmaMqttService) {
        this.tmaMqttService = null;
    }
}
