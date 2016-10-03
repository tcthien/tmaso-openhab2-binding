package com.tts.app.tmaso.binding.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.BindingConstants;
import com.tts.app.tmaso.binding.ComponentActivator;
import com.tts.app.tmaso.binding.TMAUtil;
import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.mqtt.DebugSubscriber;
import com.tts.app.tmaso.binding.mqtt.DevicePollingProducer;
import com.tts.app.tmaso.binding.mqtt.DiscoverySubscriber;
import com.tts.app.tmaso.binding.mqtt.MqttManagedThingHandler;
import com.tts.app.tmaso.binding.mqtt.MqttManagedThingHandlerImpl;
import com.tts.app.tmaso.binding.mqtt.SystemControlSubscriber;
import com.tts.app.tmaso.binding.mqtt.ThingSubscriber.ThingSubscriberCallback;
import com.tts.app.tmaso.binding.mqtt.TmaMqttService;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class TmaDeviceManagerImpl implements TmaDeviceManager, ComponentActivator {

    private static Logger logger = LoggerFactory.getLogger(TmaDeviceManagerImpl.class);

    private TmaMqttService tmaMqttService;

    private DiscoverySubscriber discoverySubscriber;
    private DebugSubscriber debugSubscriber;
    private SystemControlSubscriber systemSubscriber;
    private DevicePollingProducer pollingProducer;

    private Map<String, Object> lockObjs = new HashMap<>();

    private Map<String, Integer> pingSents = new HashMap<>();
    private Map<String, Integer> pongReplies = new HashMap<>();

    private Object lockDevice = new Object(); // Protects for device map
    private Map<String, ManagedDevice> devices = new HashMap<>();

    private Object lockHandler = new Object();
    private Map<String, MqttManagedThingHandler> deviceHandler = new HashMap<>();// Protected by m_lockHandler

    private Map<String, TmaThingHandler> thingHandlers = new HashMap<>();

    static protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("ping");

    private boolean running = true;
    protected Object lockPing = new Object();

    @Override
    public void debug() {
        logger.info("TmaDeviceManagerImpl.debug(): ---------------------------------------------");
        logger.info("    Binding Configurations:");
        debugBindingConfigurations();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.devices:");
        debugDevices();
        logger.info("");

        logger.info("    TmaDeviceManagerImpl.pings:");
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
        synchronized (lockPing) {
            for (Entry<String, Integer> entry : pingSents.entrySet()) {
                Integer ping = entry.getValue();
                Integer pong = pongReplies.get(entry.getKey()) != null ? pongReplies.get(entry.getKey()) : 0;
                logger.info("        {}- Ping: {}; Pong: {}", entry.getKey(), ping, pong);
            }
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
        synchronized (lockDevice) {
            List<ManagedDevice> rs = new ArrayList<>();
            for (Entry<String, ManagedDevice> entry : devices.entrySet()) {
                if (entry.getValue() != null) {
                    rs.add(entry.getValue());
                }
            }
            return rs;
        }
    }

    private void checkAndRegisterMqttThingHandler(final ManagedDevice device) {
        checkAndUnregisterMqttThingHandler(device.getUid());

        MqttManagedThingHandler handler = MqttManagedThingHandlerImpl.manage(TmaDeviceManagerImpl.this, device,
                new ThingSubscriberCallback() {

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
        synchronized (lockHandler) {
            deviceHandler.put(device.getUid(), handler);
        }
        handler.initialize();
    }

    private void checkAndUnregisterMqttThingHandler(String deviceUid) {
        MqttManagedThingHandler handler = deviceHandler.get(deviceUid);
        if (handler != null) {
            synchronized (lockHandler) {
                handler = deviceHandler.get(deviceUid);
                if (handler != null) {
                    // Something existed in the system => unregister
                    handler.unitialize();
                    deviceHandler.remove(deviceUid);
                }
            }
        }
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
        if (device.getUid() == null) {
            return;
        }
        register(device, true, true);

    }

    @Override
    public void reset(String deviceUid) {
        Object lock = null;
        synchronized (this) {
            lock = lockObjs.get(deviceUid);
            if (lock == null) {
                lock = new Object();
                lockObjs.put(deviceUid, lock);
            }
        }
        synchronized (lock) {
            TmaThingHandler tmaThingHandler = thingHandlers.get(deviceUid);
            if (tmaThingHandler != null) {
                tmaThingHandler.thingOffline();
            }

            checkAndUnregisterMqttThingHandler(deviceUid);

            synchronized (lockDevice) {
                devices.remove(deviceUid);
            }

            pingSents.remove(deviceUid);
            pongReplies.remove(deviceUid);
        }
    }

    private void register(ManagedDevice device, boolean needLockDevice, boolean needLockHandler) {
        Object lock = null;
        synchronized (this) {
            lock = lockObjs.get(device.getUid());
            if (lock == null) {
                lock = new Object();
                lockObjs.put(device.getUid(), lock);
            }
        }
        synchronized (lock) {
            logger.info("Register MQTT based thing: {}", device.getUid());

            if (needLockDevice) {
                synchronized (lockDevice) {
                    devices.put(device.getUid(), device);
                }
            } else {
                devices.put(device.getUid(), device);
            }

            if (needLockHandler) {
                // If having something new registered to the system => manage it
                synchronized (lockHandler) {
                    checkAndRegisterMqttThingHandler(device);
                }
            } else {
                checkAndRegisterMqttThingHandler(device);
            }

            TmaThingHandler tmaThingHandler = thingHandlers.get(device.getUid());
            if (tmaThingHandler != null) {
                tmaThingHandler.thingOnline();
            }
        }
    }

    @Override
    public void pong(ManagedDevice device) {
        // Increase pong count
        synchronized (lockPing) {
            Integer pongCount = pongReplies.get(device.getUid());
            if (pongCount == null) {
                pongCount = 0;
            }
            pongCount++;
            pongReplies.put(device.getUid(), pongCount);
        }

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
        systemSubscriber = new SystemControlSubscriber(this);
        pollingProducer = new DevicePollingProducer(this);

        tmaMqttService.register(discoverySubscriber);
        tmaMqttService.register(debugSubscriber);
        tmaMqttService.register(systemSubscriber);
        tmaMqttService.register(pollingProducer);

        // Initialize ping thread
        TMAUtil.runThreadSafe(new Runnable() {

            @Override
            public void run() {
                while (running) {
                    try {
                        synchronized (lockPing) {
                            ping();
                        }
                        Thread.sleep(BindingConstants.TIME_PING * 1000);
                    } catch (Exception e) {
                    }
                }
            }

        });
    }

    @Override
    public void ping() {
        // Check if thing is online
        Set<String> uids = new HashSet<>();
        for (Entry<String, Integer> entry : pingSents.entrySet()) {
            String deviceUid = entry.getKey();

            Integer pingSent = entry.getValue();
            Integer pongReply = pongReplies.get(deviceUid) != null ? pongReplies.get(deviceUid) : 0;
            if (pingSent != null && pongReply != null && (pingSent - pongReply >= 4)) {
                // If there is no pong rely after 2 minutes (4 times of ping)
                uids.add(deviceUid);
            }
        }

        for (String uid : uids) {
            logger.warn("PING: Thing {} is going to be OFFLINE", uid);
            reset(uid);
        }

        try {
            pollingProducer.ping();
            List<ManagedDevice> availableDevices = getAvailableDevices();
            for (ManagedDevice device : availableDevices) {
                if (device == null) {
                    continue;
                }
                Integer pingSent = pingSents.get(device.getUid());
                if (pingSent == null) {
                    pingSent = 0;
                }
                pingSent++;
                pingSents.put(device.getUid(), pingSent);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    @Override
    public void deactivate(ComponentContext context) {
        // Un-initialize Service
        logger.info("'{}' is uninitializing...", getClass().getName());
        tmaMqttService.unregister(discoverySubscriber);
        tmaMqttService.unregister(debugSubscriber);
        tmaMqttService.unregister(systemSubscriber);
        tmaMqttService.unregister(pollingProducer);

        synchronized (lockHandler) {
            for (MqttManagedThingHandler handler : deviceHandler.values()) {
                handler.unitialize();
            }
            deviceHandler.clear();
        }

        thingHandlers.clear();
        running = false;
        lockObjs.clear();
        TMAUtil.destroy();
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
