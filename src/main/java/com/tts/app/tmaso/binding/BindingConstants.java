/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.tts.app.tmaso.binding;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.tts.app.tmaso.binding.type.MessageHelper;

/**
 * The {@link ntpBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
public class BindingConstants {

    public static final String BINDING_ID = "tmaso";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_NONE = new ThingTypeUID(BINDING_ID, "none");
    public final static ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");
    public final static ThingTypeUID THING_TYPE_DOOR = new ThingTypeUID(BINDING_ID, "door");
    public final static ThingTypeUID THING_TYPE_LCD = new ThingTypeUID(BINDING_ID, "lcd");
    public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public final static ThingTypeUID THING_TYPE_STATUS_SENSOR = new ThingTypeUID(BINDING_ID, "status");
    public final static ThingTypeUID THING_TYPE_VALUE_SENSOR = new ThingTypeUID(BINDING_ID, "value");
    public final static ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public final static ThingTypeUID THING_TYPE_GAS = new ThingTypeUID(BINDING_ID, "gas");
    public final static ThingTypeUID THING_TYPE_RFID = new ThingTypeUID(BINDING_ID, "rfid");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    public final static ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");

    public final static ThingTypeUID THING_TYPE_MARY_TTS = new ThingTypeUID(BINDING_ID, "maryTTS");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = MessageHelper.asSet(THING_TYPE_ALARM,
            THING_TYPE_DOOR, THING_TYPE_LCD, THING_TYPE_LIGHT, THING_TYPE_STATUS_SENSOR, THING_TYPE_VALUE_SENSOR,
            THING_TYPE_CONTACT, THING_TYPE_GAS, THING_TYPE_RFID, THING_TYPE_SWITCH, THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_MARY_TTS);

    public static final long TIME_DISCOVERY = 30;// In seconds
    public static long TIME_PING = 30;// In seconds

    public static final String MARY_TTS_SERVER = "http://localhost:59125";
    public static final String MARY_TTS_DEFAULT_PARAM = "&audioformats=WAVE_FILE&effect_Volume_parameters=amount%3A2.0%3B&effect_Volume_default=Default&effect_Volume_help=Help&effect_TractScaler_parameters=amount%3A1.5%3B&effect_TractScaler_default=Default&effect_TractScaler_help=Help&effect_F0Scale_parameters=f0Scale%3A2.0%3B&effect_F0Scale_default=Default&effect_F0Scale_help=Help&effect_F0Add_parameters=f0Add%3A50.0%3B&effect_F0Add_default=Default&effect_F0Add_help=Help&effect_Rate_parameters=durScale%3A1.5%3B&effect_Rate_default=Default&effect_Rate_help=Help&effect_Robot_parameters=amount%3A100.0%3B&effect_Robot_default=Default&effect_Robot_help=Help&effect_Whisper_parameters=amount%3A100.0%3B&effect_Whisper_default=Default&effect_Whisper_help=Help&effect_Stadium_parameters=amount%3A100.0&effect_Stadium_default=Default&effect_Stadium_help=Help&effect_Chorus_parameters=delay1%3A466%3Bamp1%3A0.54%3Bdelay2%3A600%3Bamp2%3A-0.10%3Bdelay3%3A250%3Bamp3%3A0.30&effect_Chorus_default=Default&effect_Chorus_help=Help&effect_FIRFilter_parameters=type%3A3%3Bfc1%3A500.0%3Bfc2%3A2000.0&effect_FIRFilter_default=Default&effect_FIRFilter_help=Help&effect_JetPilot_default=Default&effect_JetPilot_help=Help&VOICE_SELECTIONS=cmu-slt-hsmm%20en_US%20female%20hmm&AUDIO_OUT=WAVE_FILE&LOCALE=en_US&VOICE=cmu-slt-hsmm&AUDIO=WAVE_FILE";
}
