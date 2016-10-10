/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding.espmqtt.handler;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;

import org.eclipse.smarthome.config.core.Configuration;
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

    private static final String CONFIG_MARYTTS_SERVER = "maryttsServer";
    private static final String CONFIG_WAKEUP_TIME = "wakeUpAlarm";
    private static final String CONFIG_WAKEUP_MSG = "wakeUpAlarmMsg";

    private static Logger logger = LoggerFactory.getLogger(MaryTTSThingHandler.class);

    private DiscoveryServiceRegistry discoveryServiceRegistry;

    // Setting
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private String maryttsServer = null;

    private Timer wakeUptimer;
    private int wakeupHour;
    private int wakeupMinute;
    private String wakeUpMsg;

    private Timer sleeptimer;
    private String sleepMsg;
    private int sleepHour;
    private int sleepMinute;

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

    private void invokeMaryTTS(String val) throws Exception {
        String text = val.replace(" ", "%20");
        String voiceUrl = maryttsServer + "/process?INPUT_TEXT=" + text + BindingConstants.MARY_TTS_DEFAULT_PARAM;
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

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        parseConfig(config);
        discoveryServiceRegistry.addDiscoveryListener(this);
        thingOnline();

        // Wake up timer
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, wakeupHour);
        today.set(Calendar.MINUTE, wakeupMinute);
        today.set(Calendar.SECOND, 0);
        if (wakeUptimer != null) {
            wakeUptimer.cancel();
        }
        wakeUptimer = new Timer();
        wakeUptimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    Date now = new Date();
                    if (now.getHours() == wakeupHour && now.getMinutes() == wakeupMinute) {
                        invokeMaryTTS(wakeUpMsg);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));

        // Sleep timer
        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, sleepHour);
        today.set(Calendar.MINUTE, sleepMinute);
        today.set(Calendar.SECOND, 0);
        if (sleeptimer != null) {
            sleeptimer.cancel();
        }
        sleeptimer = new Timer();
        sleeptimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    Date now = new Date();
                    if (now.getHours() == sleepHour && now.getMinutes() == sleepMinute) {
                        invokeMaryTTS(sleepMsg);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
    }

    private void parseConfig(Configuration config) {
        maryttsServer = (String) config.get(CONFIG_MARYTTS_SERVER);

        wakeUpMsg = (String) config.get(CONFIG_WAKEUP_MSG);
        String wakeUpAlarmString = (String) config.get(CONFIG_WAKEUP_TIME);
        String[] arr = wakeUpAlarmString.split("\\:");
        wakeupHour = Integer.parseInt(arr[0]);
        wakeupMinute = Integer.parseInt(arr[1]);

        sleepMsg = (String) config.get("sleepAlarmMsg");
        String sleepAlarmString = (String) config.get("sleepAlarm");
        arr = sleepAlarmString.split("\\:");
        sleepHour = Integer.parseInt(arr[0]);
        sleepMinute = Integer.parseInt(arr[1]);
    }

    @Override
    public void dispose() {
        discoveryServiceRegistry.removeDiscoveryListener(this);
        if (wakeUptimer != null) {
            wakeUptimer.cancel();
        }
        if (sleeptimer != null) {
            sleeptimer.cancel();
        }
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
