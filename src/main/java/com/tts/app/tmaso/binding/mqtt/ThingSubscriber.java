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
import com.tts.app.tmaso.binding.type.MqttAction;

public class ThingSubscriber extends TmaMqttSubscriber {

    public static interface ThingSubscriberCallback {

        Thing getThing(ManagedDevice device);

        void updateStatePublic(ManagedDevice device, ChannelUID channelUid, State value);

    }

    private static Logger logger = LoggerFactory.getLogger(ThingSubscriber.class);

    private ThingSubscriberCallback callback;
    private ManagedDevice device;

    public ThingSubscriber(ThingSubscriberCallback callback, ManagedDevice device) {
        super(device.getPath() + "/broadcast");
        this.callback = callback;
        this.device = device;
    }

    @Override
    protected void processMessage(MqttMessage mqttMessage) {
        Thing thing = callback.getThing(device);
        if (thing != null) {
            logger.info("processMessage for {}", mqttMessage.toString());
            if (mqttMessage.getBody() instanceof GetSetMessageBody) {
                GetSetMessageBody body = mqttMessage.getBody();
                for (Entry<String, State> entry : body.channels().entrySet()) {
                    ChannelUID channelUid = new ChannelUID(thing.getUID(), entry.getKey());
                    callback.updateStatePublic(device, channelUid, entry.getValue());
                    device.setChannel(entry.getKey(), entry.getValue());
                }
            }
        } else {
            logger.warn("There's no {} so message {} has been ignored", TmaThingHandler.class.getSimpleName(),
                    mqttMessage.toString());
        }
    }

    @Override
    protected MqttMessageBody parseMessageBody(MqttMessage msg, String[] bodyArr) {
        // Firmware send MQTT Set packet
        if (msg.getAction().equals(MqttAction.SET) || msg.getAction().equals(MqttAction.GET)
                || msg.getAction().equals(MqttAction.SET_BULK) || msg.getAction().equals(MqttAction.GET_BULK)) {
            return parseSetGetMessage(device, msg, bodyArr);
        }

        return null;
    }
}
