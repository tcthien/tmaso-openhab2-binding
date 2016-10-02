package com.tts.app.tmaso.binding;

public class TMAUtil {

    public static String toString(Object... objects) {
        StringBuilder sb = new StringBuilder();
        String concat = "";
        for (Object obj : objects) {
            sb.append(concat).append(obj);
            concat = ",";
        }
        return sb.toString();
    }

}
