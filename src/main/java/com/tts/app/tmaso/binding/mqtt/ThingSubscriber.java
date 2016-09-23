package com.tts.app.tmaso.binding.mqtt;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tts.app.tmaso.binding.device.TmaDevice;
import com.tts.app.tmaso.binding.espmqtt.handler.TmaThingHandler;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessage;
import com.tts.app.tmaso.binding.mqtt.msg.MqttMessageBody;
import com.tts.app.tmaso.binding.mqtt.msg.SetMessageBody;
import com.tts.app.tmaso.binding.type.ChannelPair;
import com.tts.app.tmaso.binding.type.ChannelType;
import com.tts.app.tmaso.binding.type.MessageHelper;
import com.tts.app.tmaso.binding.type.MqttAction;
import com.tts.app.tmaso.binding.type.MqttConstants;

public class ThingSubscriber extends TmaMqttSubscriber {

    private static Logger logger = LoggerFactory.getLogger(ThingSubscriber.class);

    private TmaThingHandler thingHandler;
    private TmaDevice device;

    public ThingSubscriber(TmaThingHandler tmaThingHandler, TmaDevice device) {
        super(device.getPath() + "/broadcast");
        this.thingHandler = tmaThingHandler;
        this.device = device;
    }

    @Override
    protected void processMessage(MqttMessage mqttMessage) {
        Thing thing = thingHandler.getThing();

        if (mqttMessage.getBody() instanceof SetMessageBody) {
            SetMessageBody body = mqttMessage.getBody();
            String channelName = body.channelName();

            ChannelUID channelUid = new ChannelUID(thing.getUID(), channelName);
            thingHandler.updateStatePublic(channelUid, body.value());
        }
    }

    @Override
    protected MqttMessageBody parseMessageBody(MqttMessage rs, String[] bodyArr) {
        String channelName = (bodyArr.length == 1) ? MqttConstants.CHANNEL_NAME_DEFAULT : bodyArr[0];
        String value = (bodyArr.length == 1) ? bodyArr[0] : bodyArr[1];

        ChannelPair channel = device.getChannel(channelName);
        if (channel == null) {
            logger.error("Invalid channel name '{}' sent to device '{}'", channelName, device.getUid());
            return null;
        }

        // Firmware send MQTT Set packet
        if (rs.getAction().equals(MqttAction.SET)) {
            SetMessageBody body = MqttMessageBody.setBody();
            body.channelName(channelName);
            if (channel.getValue().equals(ChannelType.Status)) {
                body.value(MessageHelper.convertToESHOnOff(value.trim()));
            } else {
                body.value(new StringType(value.trim()));
            }
            return body.cast();
        }

        return null;
    }
}
