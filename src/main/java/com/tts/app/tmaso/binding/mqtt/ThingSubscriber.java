package com.tts.app.tmaso.binding.mqtt;

import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.device.ManagedDevice;
import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.mqtt.msg.GetSetMessageBody;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessageBody;
import com.tts.app.tmaso.binding.type.ChannelMetaData;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class ThingSubscriber extends TmaMqttSubscriber {

    private static Logger logger = LoggerFactory.getLogger(ThingSubscriber.class);

    private TmaThingHandler thingHandler;
    private ManagedDevice device;

    public ThingSubscriber(TmaThingHandler tmaThingHandler, ManagedDevice device) {
        super(device.getPath() + "/broadcast");
        this.thingHandler = tmaThingHandler;
        this.device = device;
    }

    @Override
    protected void processMessage(MqttMessage mqttMessage) {
        Thing thing = thingHandler.getThing();

        if (mqttMessage.getBody() instanceof GetSetMessageBody) {
            GetSetMessageBody body = mqttMessage.getBody();
            for (Entry<String, State> entry : body.channels().entrySet()) {
                ChannelUID channelUid = new ChannelUID(thing.getUID(), entry.getKey());
                thingHandler.updateStatePublic(channelUid, entry.getValue());
                device.setChannel(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected MqttMessageBody parseMessageBody(MqttMessage msg, String[] bodyArr) {
        String channelName = (bodyArr.length == 1) ? MqttConstants.CHANNEL_NAME_DEFAULT : bodyArr[0];
        String value = (bodyArr.length == 1) ? bodyArr[0] : bodyArr[1];

        ChannelMetaData channel = device.getChannelMetaData(channelName);
        if (channel == null) {
            logger.error("Invalid channel name '{}' sent to device '{}'", channelName, device.getUid());
            return null;
        }

        // Firmware send MQTT Set packet
        if (msg.getAction().equals(MqttAction.SET) || msg.getAction().equals(MqttAction.GET)
                || msg.getAction().equals(MqttAction.SET_BULK) || msg.getAction().equals(MqttAction.GET_BULK)) {
            return parseSetGetMessage(device, msg, bodyArr);
        }

        return null;
    }
}
