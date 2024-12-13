package ru.webdevpet.server.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigValidator {

    public static void validate(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (isValidPort(config.getHttpPort())) {
            throw new IllegalArgumentException("Invalid HTTP port: " + config.getHttpPort());
        }
        if (isValidPort(config.getWebsocketPort())) {
            throw new IllegalArgumentException("Invalid WebSocket port: " + config.getWebsocketPort());
        }


       if(!isValidToken(config.getToken())) {
           throw new IllegalArgumentException("Invalid token format: " + config.getToken());
       }
    }


    private static boolean isValidPort(int port) {
        return port < 1 || port > 65535;
    }
    private static boolean isValidToken(String token) {
        if (token == null || token.length() != 32) {
            return false;
        }

        return token.matches("[0-9a-fA-F]{32}");
    }



}

