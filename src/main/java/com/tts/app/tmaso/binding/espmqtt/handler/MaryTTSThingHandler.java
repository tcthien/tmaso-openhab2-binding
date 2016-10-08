/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding.espmqtt.handler;

import java.net.URL;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.BindingConstants;

/**
 *
 * The NTP Refresh Service polls the configured timeserver with a configurable
 * interval and posts a new event of type ({@link DateTimeType}.
 *
 * The {@link MaryTTSThingHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution OH2 ntp binding
 * @author Thomas.Eichstaedt-Engelen OH1 ntp binding (getTime routine)
 * @author Markus Rathgeb - Add locale provider
 */

public class MaryTTSThingHandler extends BaseThingHandler implements DiscoveryListener {

    private static Logger logger = LoggerFactory.getLogger(MaryTTSThingHandler.class);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    // ThingSubscriber thingSubscriber;
    // ThingProducer thingProducer;

    public MaryTTSThingHandler(final Thing thing, DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "# " + getThing().getUID() + " - " + getThing().getStatus();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        thingOnline();

        try {
            // Update State --------------------
            if (command instanceof StringType) {
                StringType val = (StringType) command;
                // Invoke to maryTTS Server
                invokeMaryTTS(val.toString());
            }
        } catch (Exception e) {
            logger.error("", e);
            thingOffline();
        }
    }

    private static void invokeMaryTTS(String val) throws Exception {
        String text = val.replace(" ", "%20");
        String voiceUrl = BindingConstants.MARY_TTS_SERVER + "/process?INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&INPUT_TEXT="
                + text + BindingConstants.MARY_TTS_DEFAULT_PARAM;
        // Play WAV from URL
        logger.info("MaryTTS: {}", voiceUrl);
        URL url = new URL(voiceUrl);

        // getAudioInputStream() also accepts a File or InputStream
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        AudioFormat format = ais.getFormat();
        Info info = new DataLine.Info(Clip.class, format);

        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(ais);
        clip.start();
    }

    public static void main(String[] args) throws Exception {
        invokeMaryTTS("Hello World");
        Thread.sleep(10000);
    }

    @Override
    public void initialize() {
        discoveryServiceRegistry.addDiscoveryListener(this);
        thingOnline();
    }

    @Override
    public void dispose() {
        discoveryServiceRegistry.removeDiscoveryListener(this);
        super.dispose();
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            thingOnline();
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            logger.debug("Setting status for thing '{}' to OFFLINE", getThing().getUID());
            thingOffline();
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

    public void thingOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void thingOffline() {
        updateStatus(ThingStatus.OFFLINE);
    }
}
