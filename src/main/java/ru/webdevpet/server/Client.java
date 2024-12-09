package ru.webdevpet.server;

import org.java_websocket.WebSocket;

public class Client {
    private WebSocket socket;
    private String channel;
    public Client(WebSocket socket, String channel) {
        this.socket = socket;
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public WebSocket getSocket() {
        return socket;
    }
}
