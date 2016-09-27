package com.tts.app.tmaso.binding.mqtt.msg;

import java.util.Map;

import org.eclipse.smarthome.core.types.State;

public interface GetSetMessageBody extends MessageBody {
    void channel(String channelName, State channelValue);

    State channelValue(String channelName);

    Map<String, State> channels();
}
