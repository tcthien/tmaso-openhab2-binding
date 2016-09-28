/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding.espmqtt.handler;

import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.ComponentActivator;
import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.device.TmaDeviceManager;
import com.tts.app.tmaso.binding.mqtt.ThingProducer;
import com.tts.app.tmaso.binding.mqtt.ThingSubscriber;
import com.tts.app.tmaso.binding.mqtt.TmaMqttService;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.ChannelType;
import com.tts.app.tmaso.binding.type.MqttAction;

/**
 *
 * The NTP Refresh Service polls the configured timeserver with a configurable
 * interval and posts a new event of type ({@link DateTimeType}.
 *
 * The {@link TmaThingHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution OH2 ntp binding
 * @author Thomas.Eichstaedt-Engelen OH1 ntp binding (getTime routine)
 * @author Markus Rathgeb - Add locale provider
 */

public class TmaThingHandler extends BaseThingHandler implements DiscoveryListener, ComponentActivator {

    private static Logger logger = LoggerFactory.getLogger(TmaThingHandler.class);
    private Object objLock = new Object();

    private final LocaleProvider localeProvider;
    private TmaDeviceManager deviceManager;
    private TmaMqttService tmaMqttService;

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    ThingSubscriber thingSubscriber;
    ThingProducer thingProducer;

    private boolean running = true;
    private boolean subscribed = false;
    private Thread thread;

    public TmaThingHandler(final Thing thing, final LocaleProvider localeProvider, TmaDeviceManager deviceManager,
            TmaMqttService tmaMqttService, DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);
        this.localeProvider = localeProvider;
        this.deviceManager = deviceManager;
        this.tmaMqttService = tmaMqttService;
        this.discoveryServiceRegistry = discoveryServiceRegistry;
        initializeResource();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ManagedDevice device = getCurrentDevice();
        if (device == null) {
            return;
        }
        initializeResource();
        try {
            // Publish command to MQTT
            thingProducer.publish(command, channelUID.getId(), MqttAction.SET);
            // Update State
            ChannelMetaData channel = device.getChannelMetaData(channelUID.getId());
            if (channel.getValue().equals(ChannelType.OnOff) || channel.getValue().equals(ChannelType.Status)) {
                if (ThingProducer.isOnValue(command)) {
                    updateState(channelUID, OnOffType.ON);
                } else {
                    updateState(channelUID, OnOffType.OFF);
                }
            } else if (channel.getValue().equals(ChannelType.OpenClosed)) {
                if (ThingProducer.isOnValue(command)) {
                    updateState(channelUID, OpenClosedType.OPEN);
                } else {
                    updateState(channelUID, OpenClosedType.CLOSED);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public void initializeResource() {
        ManagedDevice device = getCurrentDevice();
        if (device == null || getThing() == null || getThing().getUID() == null) {
            return;
        }
        ThingUID uid = getThing().getUID();
        logger.debug("Initialize resource for: '{}'", uid);

        if (thingSubscriber == null) {
            synchronized (objLock) {
                if (thingSubscriber == null) {
                    thingSubscriber = new ThingSubscriber(this, device);
                    tmaMqttService.register(thingSubscriber);
                    subscribed = true;
                }
            }
        }
        if (thingProducer == null) {
            synchronized (objLock) {
                if (thingProducer == null) {
                    thingProducer = new ThingProducer(this, device);
                    tmaMqttService.register(thingProducer);
                    subscribed = true;
                }
            }
        }
    }

    public void uninitializeResource() {
        // Clean up subscriber
        ThingUID uid = getThing().getUID();
        logger.debug("Uninitialize resource for: '{}'", uid);
        if (thingSubscriber != null) {
            synchronized (objLock) {
                if (thingSubscriber != null) {
                    tmaMqttService.unregister(thingSubscriber);
                    subscribed = false;
                }
            }
        }
        if (thingProducer != null) {
            synchronized (objLock) {
                if (thingProducer != null) {
                    tmaMqttService.unregister(thingProducer);
                    subscribed = false;
                }
            }
        }
    }

    @Override
    public void initialize() {
        activate(null);
        discoveryServiceRegistry.addDiscoveryListener(this);
        updateStatus(ThingStatus.ONLINE);
        ManagedDevice device = getCurrentDevice();
        if (device != null) {
            ThingUID uid = getThing().getUID();
            for (Entry<String, State> entry : device.getChannels().entrySet()) {
                ChannelUID channelUid = new ChannelUID(uid, entry.getKey());
                updateStatePublic(channelUid, entry.getValue());
            }
        }
    }

    private ManagedDevice getCurrentDevice() {
        ThingUID uid = getThing().getUID();
        String deviceUid = uid.getId();

        ManagedDevice device = deviceManager.getDevice(deviceUid);
        return device;
    }

    @Override
    public void dispose() {
        deactivate(null);
        discoveryServiceRegistry.removeDiscoveryListener(this);
        super.dispose();
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.ONLINE);
            initializeResource();
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            logger.debug("Setting status for thing '{}' to OFFLINE", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE);
            uninitializeResource();
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateStatePublic(ChannelUID channelUid, State state) {
        updateState(channelUid, state);
        postCommand(channelUid, RefreshType.REFRESH);
    }

    @Override
    public void activate(ComponentContext context) {
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (running && !isSubscribed()) {
                    initializeResource();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        thread.run();
        initializeResource();
    }

    @Override
    public void deactivate(ComponentContext context) {
        running = false;
        uninitializeResource();
    }

    public boolean isSubscribed() {
        return subscribed;
    }
}
