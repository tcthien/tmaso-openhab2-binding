package com.tts.app.tmaso.binding.mqtt.msg;

import com.tts.app.tmaso.binding.type.MqttAction;

public class MqttMessage {

    private MqttAction action;
    private String uid;
    private MqttMessageBody body;

    public void setMqttAction(MqttAction action) {
        this.action = action;
    }

    public MqttAction getAction() {
        return action;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setBody(MqttMessageBody msgBody) {
        this.body = msgBody;
    }

    public MqttMessageBody getBody() {
        return body;
    }

}
