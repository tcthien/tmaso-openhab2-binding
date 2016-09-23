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
    public final static ThingTypeUID THING_TYPE_STATUS_SENSOR = new ThingTypeUID(BINDING_ID, "statussensor");
    public final static ThingTypeUID THING_TYPE_VALUE_SENSOR = new ThingTypeUID(BINDING_ID, "valuesensor");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = MessageHelper.asSet(THING_TYPE_ALARM,
            THING_TYPE_DOOR, THING_TYPE_LCD, THING_TYPE_LIGHT, THING_TYPE_STATUS_SENSOR, THING_TYPE_VALUE_SENSOR);

    public static final long TIME_DISCOVERY = 30;// In seconds
    public static long TIME_PING = 5;// 5 minutes
}
