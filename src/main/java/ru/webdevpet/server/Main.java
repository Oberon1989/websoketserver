package ru.webdevpet.server;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.print("Hello and 1!");
        WebSocketHttpServer server = new WebSocketHttpServer();
        server.start();

    }
}