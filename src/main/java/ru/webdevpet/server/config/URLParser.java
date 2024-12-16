package ru.webdevpet.server.config;

import org.java_websocket.handshake.ClientHandshake;

public class URLParser {
    public static String getParameter(String key, ClientHandshake handshake) {
        try {
            String query = handshake.getResourceDescriptor().split("\\?")[1];

            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(key)) {
                    return keyValue[1];
                }
            }
            return null;
        }
        catch (Exception e) {
            return null;
        }

    }
}
