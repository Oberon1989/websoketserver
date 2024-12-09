package ru.webdevpet.server;

import ru.webdevpet.server.config.Config;

public class Main {
    public static void main(String[] args) throws Exception {
        String filepath = "server.conf";
        try {
            Config config = Config.getCurrentConfig(filepath);
            if (config == null) {
                System.out.println("config file " + filepath + " corrupt or not exist. Please check working directory");
                return;
            }
            WebSocketHttpServer server = new WebSocketHttpServer(config);
            server.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return;
        }
    }
}