package com.tts.app.tmaso.binding.mqtt;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.ChannelType;
import com.tts.app.tmaso.binding.type.MqttAction;

public class ThingProducer extends TmaMqttProducer {

    private static Logger logger = LoggerFactory.getLogger(ThingProducer.class);

    private TmaThingHandler thingHandler;
    private ManagedDevice device;

    public ThingProducer(TmaThingHandler tmaThingHandler, ManagedDevice device) {
        super(device.getPath());
        this.thingHandler = tmaThingHandler;
        this.device = device;
    }

    public void publish(Command command, String channelName, MqttAction mqttAction) throws Exception {
        ChannelMetaData channel = device.getChannelMetaData(channelName);
        if (channel == null) {
            logger.error("Invalid channel name '{}' sent to device '{}'", channelName, device.getUid());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(mqttAction.toString());
        sb.append(";").append(device.getUid());
        sb.append(";").append(channelName);
        if (channel.getValue().equals(ChannelType.OnOff) || channel.getValue().equals(ChannelType.Status)) {
            if (isOnValue(command)) {
                sb.append(";").append(OnOffType.ON);
            } else {
                sb.append(";").append(OnOffType.OFF);
            }
        } else if (channel.getValue().equals(ChannelType.OpenClosed)) {
            if (isOnValue(command)) {
                sb.append(";").append(OpenClosedType.OPEN);
            } else {
                sb.append(";").append(OpenClosedType.CLOSED);
            }
        }
        publish(sb.toString());
    }

    public static boolean isOnValue(Command command) {
        if (command == null) {
            return false;
        }
        if (OnOffType.ON.equals(command) || UpDownType.UP.equals(command) || OpenClosedType.OPEN.equals(command)) {
            return true;
        }
        String val = command.toString();
        return val.equalsIgnoreCase("on") || val.equalsIgnoreCase("up") || val.equalsIgnoreCase("open")
                || val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1");
    }
}
