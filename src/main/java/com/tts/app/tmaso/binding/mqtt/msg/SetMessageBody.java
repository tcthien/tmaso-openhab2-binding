package com.tts.app.tmaso.binding.mqtt.msg;

import org.eclipse.smarthome.core.types.State;

public interface SetMessageBody extends MessageBody {
    void channelName(String channelName);

    void value(State value);

    String channelName();

    State value();
}
