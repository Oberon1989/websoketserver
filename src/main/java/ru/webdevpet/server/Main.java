package ru.webdevpet.server;

import ru.webdevpet.server.config.Config;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            throw new Exception("not arg on auth");
        }

        boolean auth = parseBoolean(args[0]);
        String filepath = "server.conf";
        try {
            Config config = Config.getCurrentConfig(filepath);
            if (config == null) {
                System.out.println("config file " + filepath + " corrupt or not exist. Please check working directory");
                return;
            }
            WebSocketHttpServer server = new WebSocketHttpServer(config,auth);
            server.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static boolean parseBoolean(String str){
        if(str==null) return false;
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("1");
    }
}