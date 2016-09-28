package com.tts.app.tmaso.binding.type;

public enum ChannelType {
    Status,
    OnOff,
    OpenClosed,
    Number,
    String;

    public static ChannelType fromString(String value) {
        switch (value) {
            case "status":
                return Status;
            case "onoff":
                return OnOff;
            case "openclosed":
                return OpenClosed;
            case "number":
                return Number;
            default:
                return String;
        }
    }

    @Override
    public java.lang.String toString() {
        return super.toString().toLowerCase();
    }
}
