package com.tts.app.tmaso.binding.device;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.BindingConstants;
import com.tts.app.tmaso.binding.ComponentActivator;
import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.mqtt.DebugSubscriber;
import com.tts.app.tmaso.binding.mqtt.DevicePollingProducer;
import com.tts.app.tmaso.binding.mqtt.DiscoverySubscriber;
import com.tts.app.tmaso.binding.mqtt.MqttManagedThingHandler;
import com.tts.app.tmaso.binding.mqtt.MqttManagedThingHandlerImpl;
import com.tts.app.tmaso.binding.mqtt.ThingSubscriber.ThingSubscriberCallback;
import com.tts.app.tmaso.binding.mqtt.TmaMqttService;
import com.tts.app.tmaso.binding.type.DeviceStatus;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class TmaDeviceManagerImpl implements TmaDeviceManager, ComponentActivator {

    private static TmaDeviceManagerImpl INSTANCE = null;

    private static Logger logger = LoggerFactory.getLogger(TmaDeviceManagerImpl.class);

    private TmaMqttService tmaMqttService;

    private DiscoverySubscriber discoverySubscriber;
    private DebugSubscriber debugSubscriber;
    private DevicePollingProducer pollingProducer;

    private Object lockDevice = new Object(); // Protects for device map
    private Map<String, ManagedDevice> devices = new ConcurrentHashMap<>();
    private Map<String, Date> deviceUpdatedTime = new ConcurrentHashMap<>();

    private Object lockHandler = new Object();
    private Map<String, MqttManagedThingHandler> deviceHandler = new HashMap<>();// Protected by m_lockHandler

    private Map<String, TmaThingHandler> thingHandlers = new ConcurrentHashMap<>();

    public TmaDeviceManagerImpl() {
        INSTANCE = this;
    }

    public static synchronized TmaDeviceManagerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void debug() {
        logger.info("TmaDeviceManagerImpl.debug(): ---------------------------------------------");
        logger.info("    Binding Configurations:");
        debugBindingConfigurations();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.devices:");
        debugDevices();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.deviceUpdatedTime:");
        debugDeviceUpdatedTime();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.deviceHandler:");
        debugDeviceHandler();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.thingHandlers:");
        debugThingHandlers();
        logger.info("");
        logger.info("---------------------------------------------------------------------------");
    }

    private void debugBindingConfigurations() {
        logger.info("        {}: {}", "Discover topic", MqttConstants.TOPIC_DISCOVERY);
        logger.info("        {}: {}", "Ping topic", MqttConstants.TOPIC_PING);
        logger.info("        {}: {}", "Debug topic", MqttConstants.TOPIC_DEBUG);
        logger.info("        {}: {}", "Time Discover (seconds)", BindingConstants.TIME_DISCOVERY);
        logger.info("        {}: {}", "Time Ping (seconds)", BindingConstants.TIME_PING);
        logger.info("        {}: {}", "MQTT Broker Name", MqttConstants.BROKER_NAME);
    }

    private void debugThingHandlers() {
        for (Entry<String, TmaThingHandler> entry : thingHandlers.entrySet()) {
            logger.info("        {}: {}", entry.getKey(), entry.getValue());
        }
    }

    private void debugDeviceHandler() {
        for (Entry<String, MqttManagedThingHandler> entry : deviceHandler.entrySet()) {
            logger.info("        {}: {}", entry.getKey(), entry.getValue());
        }
    }

    private void debugDeviceUpdatedTime() {
        for (Entry<String, Date> entry : deviceUpdatedTime.entrySet()) {
            logger.info("        {}: {}", entry.getKey(), entry.getValue());
        }
    }

    private void debugDevices() {
        for (Entry<String, ManagedDevice> entry : devices.entrySet()) {
            logger.info("        {}: {}", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void manageThingHandler(String deviceUid, TmaThingHandler thingHandler) {
        thingHandlers.put(deviceUid, thingHandler);
    }

    @Override
    public ManagedDevice getDevice(String deviceUid) {
        return devices.get(deviceUid);
    }

    @Override
    public List<ManagedDevice> getAvailableDevices() {
        List<ManagedDevice> rs = new ArrayList<>();
        synchronized (lockDevice) {
            for (ManagedDevice device : devices.values()) {
                if (getStatus(device.getUid()).equals(DeviceStatus.ONLINE)) {
                    rs.add(device);
                }
            }
        }
        return rs;
    }

    @Override
    public DeviceStatus getStatus(String deviceUid) {
        Date date = deviceUpdatedTime.get(deviceUid);
        return date != null ? DeviceStatus.ONLINE : DeviceStatus.OFFLINE;
        // return date == null || (pollingProducer.getPingTime() != null && date.before(pollingProducer.getPingTime()))
        // ? DeviceStatus.OFFLINE : DeviceStatus.ONLINE;
    }

    private void checkAndRegister(ManagedDevice device) {
        MqttManagedThingHandler handler = deviceHandler.get(device.getUid());
        if (handler != null) {
            // Something existed in the system => unregister
            handler.unitialize();
        }
        handler = MqttManagedThingHandlerImpl.manage(this, device, new ThingSubscriberCallback() {

            @Override
            public Thing getThing(ManagedDevice device) {
                TmaThingHandler tmaThingHandler = thingHandlers.get(device.getUid());
                if (tmaThingHandler != null) {
                    return tmaThingHandler.getThing();
                }
                return null;
            }

            @Override
            public void updateStatePublic(ManagedDevice device, ChannelUID channelUid, State state) {
                TmaThingHandler tmaThingHandler = thingHandlers.get(device.getUid());
                if (tmaThingHandler != null) {
                    tmaThingHandler.updateStatePublic(channelUid, state);
                } else {
                    logger.warn("There's no {} so update request for {}: {} has been ignored",
                            TmaThingHandler.class.getSimpleName(), channelUid, state);
                }
            }
        });

        deviceHandler.put(device.getUid(), handler);
        handler.initialize();
    }

    @Override
    public void publishMqttCommand(ManagedDevice device, Command command, String channelId, MqttAction mqttAction)
            throws Exception {
        MqttManagedThingHandler handler = deviceHandler.get(device.getUid());
        if (handler != null) {
            handler.publish(command, channelId, mqttAction);
        }
    }

    @Override
    public void register(ManagedDevice device) {
        logger.info("Register MQTT based thing: {}", device.getUid());
        register(device, true, true);

    }

    private void register(ManagedDevice device, boolean needLockDevice, boolean needLockHandler) {
        if (needLockDevice) {
            synchronized (lockDevice) {
                deviceUpdatedTime.put(device.getUid(), new Date());
                devices.put(device.getUid(), device);
            }
        } else {
            deviceUpdatedTime.put(device.getUid(), new Date());
            devices.put(device.getUid(), device);
        }

        if (needLockHandler) {
            // If having something new registered to the system => manage it
            synchronized (lockHandler) {
                checkAndRegister(device);
            }
        } else {
            checkAndRegister(device);
        }

        TmaThingHandler tmaThingHandler = thingHandlers.get(device.getUid());
        if (tmaThingHandler != null) {
            tmaThingHandler.thingOnline();
        }
    }

    @Override
    public void pong(ManagedDevice device) {
        // Device replies ping command ==> check & register
        ManagedDevice currentDevice = devices.get(device.getUid());
        if (currentDevice == null) {
            synchronized (lockDevice) {
                currentDevice = devices.get(device.getUid());
                if (currentDevice == null) {
                    // There's no device system => register
                    // we already in lockDevice synchronized so don't need to lock it again => 2nd param = false
                    register(device, false, true);
                    currentDevice = devices.get(device.getUid());
                }
            }
        }
        // currentDevice != null => update pong time
        synchronized (lockDevice) {
            deviceUpdatedTime.put(currentDevice.getUid(), new Date());
        }
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
        logger.info("'{}' is initializing...", getClass().getName());
        discoverySubscriber = new DiscoverySubscriber(this);
        debugSubscriber = new DebugSubscriber(this);
        pollingProducer = new DevicePollingProducer(this);
        pollingProducer.initialize();

        tmaMqttService.register(discoverySubscriber);
        tmaMqttService.register(debugSubscriber);
        tmaMqttService.register(pollingProducer);
    }

    @Override
    public void deactivate(ComponentContext context) {
        // Un-initialize Service
        logger.info("'{}' is uninitializing...", getClass().getName());
        pollingProducer.uninitialize();
        tmaMqttService.unregister(discoverySubscriber);
        tmaMqttService.unregister(debugSubscriber);
        tmaMqttService.unregister(pollingProducer);

        synchronized (lockHandler) {
            for (MqttManagedThingHandler handler : deviceHandler.values()) {
                handler.unitialize();
            }
            deviceHandler.clear();
        }

        thingHandlers.clear();
    }

    public void setTmaMqttService(TmaMqttService tmaMqttService) {
        this.tmaMqttService = tmaMqttService;
    }

    public void unSetTmaMqttService(TmaMqttService tmaMqttService) {
        this.tmaMqttService = null;
    }

    public TmaMqttService getTmaMqttService() {
        return tmaMqttService;
    }
}
