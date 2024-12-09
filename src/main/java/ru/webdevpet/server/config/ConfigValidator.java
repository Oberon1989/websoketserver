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
        if (!isValidPort(config.getHttpPort())) {
            throw new IllegalArgumentException("Invalid HTTP port: " + config.getHttpPort());
        }
        if (!isValidPort(config.getWebsocketPort())) {
            throw new IllegalArgumentException("Invalid WebSocket port: " + config.getWebsocketPort());
        }


        if (config.getToken() == null || config.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }



            if (!isValidUrl(config.getAuthorizeUserURL())) {
                throw new IllegalArgumentException("Invalid URL: " + config.getAuthorizeUserURL());
            }

    }


    private static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }


    private static boolean isValidUrl(String url) {

        System.out.println(url.length());

        if (url.trim().isEmpty()) {
            return false;
        }
        Pattern ipWithPortPattern = Pattern.compile(
                "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]+)?$"
        );


        Matcher matcher = ipWithPortPattern.matcher(url);
        if (matcher.matches()) {
            return true;
        }
        if (!url.contains("://")) {
            url = "http://" + url;
        }

        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}

