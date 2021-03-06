/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding.espmqtt.discover;

import static com.tts.app.tmaso.binding.BindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.BindingConstants;
import com.tts.app.tmaso.binding.ComponentActivator;
import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.device.TmaDeviceManager;

/**
 *
 * The {@link TmaDeviceDiscovery} is used to add a ntp Thing for the local time in the discovery inbox
 * *
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class TmaDeviceDiscovery extends AbstractDiscoveryService implements ComponentActivator {

    private static final String CONFIG_THING_TYPE = "thingType";
    private static final String CONFIG_THING_MQTT_TOPIC = "thingMqttTopic";
    private static final String CONFIG_THING_IP_ADDRESS = "thingIpAddress";
    private static final String CONFIG_THING_NAME = "thingName";
    private static final String CONFIG_THING_UID = "thingUid";

    private static Logger logger = LoggerFactory.getLogger(TmaDeviceDiscovery.class);

    private ScheduledFuture<?> futureCall;

    private TmaDeviceManager deviceManager;

    public TmaDeviceDiscovery() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 10);
    }

    @Override
    protected void startBackgroundDiscovery() {
        futureCall = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discoverTmaDevice();
            }
        }, BindingConstants.TIME_DISCOVERY, TimeUnit.SECONDS);
    }

    @Override
    protected void startScan() {
        discoverTmaDevice();
    }

    /**
     * Add a ntp Thing for the local time in the discovery inbox
     */
    private void discoverTmaDevice() {
        List<ManagedDevice> availableDevices = deviceManager.getAvailableDevices();

        // Device based on MQTT protocol
        for (ManagedDevice device : availableDevices) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_THING_UID, device.getUid());
            properties.put(CONFIG_THING_NAME, device.getName());
            properties.put(CONFIG_THING_IP_ADDRESS, device.getIpAddress());
            properties.put(CONFIG_THING_MQTT_TOPIC, device.getPath());
            properties.put(CONFIG_THING_TYPE, device.getDeviceType().toString());

            ThingUID uid = new ThingUID(device.getDeviceType().getThingType(), device.getUid());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(device.getName()).build();
            thingDiscovered(result);

            logger.info("Device discovered: {}", uid);
        }

        // MaryTTS
        Map<String, Object> properties = new HashMap<>();
        ThingUID uid = new ThingUID(BindingConstants.THING_TYPE_MARY_TTS, "localMaryTTS");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withLabel("Local Mary TTS").build();
        thingDiscovered(result);
    }

    @Override
    public void activate(ComponentContext context) {
        logger.info("'{}' is activating to discover smart device", getClass().getName());
    }

    @Override
    public void deactivate(ComponentContext context) {
        if (futureCall != null) {
            futureCall.cancel(false);
        }
    }

    public TmaDeviceManager getDeviceManager() {
        return deviceManager;
    }

    public void setDeviceManager(TmaDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public void unSetDeviceManager(TmaDeviceManager deviceManager) {
        this.deviceManager = null;
    }
}
