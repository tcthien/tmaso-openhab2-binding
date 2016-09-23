package com.tts.app.tmaso.binding.type;

import static com.tts.app.tmaso.binding.BindingConstants.*;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public enum DeviceType {
    None(THING_TYPE_NONE),
    Alarm(THING_TYPE_ALARM, true),
    Door(THING_TYPE_DOOR, true),
    Lcd(THING_TYPE_LCD),
    Light(THING_TYPE_LIGHT, true),
    StatusSensor(THING_TYPE_STATUS_SENSOR, true),
    ValueSensor(THING_TYPE_VALUE_SENSOR);

    private boolean isOnOffKind;
    private ThingTypeUID thingType;

    private DeviceType(ThingTypeUID thingType) {
        this.isOnOffKind = false;
        this.thingType = thingType;
    }

    private DeviceType(ThingTypeUID thingType, boolean isOnOffKind) {
        this.isOnOffKind = isOnOffKind;
        this.thingType = thingType;
    }

    public static DeviceType fromString(String val) {
        switch (val) {
            case "alarm":
                return Alarm;
            case "door":
                return Door;
            case "lcd":
                return Lcd;
            case "light":
                return Light;
            case "statussensor":
                return StatusSensor;
            case "valuesensor":
                return ValueSensor;
        }
        return None;
    }

    public boolean isStatusValue() {
        return isOnOffKind;
    }

    public ThingTypeUID getThingType() {
        return thingType;
    }

}
