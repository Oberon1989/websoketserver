package ru.webdevpet.server;

import org.java_websocket.WebSocket;

public class Client {
    private final WebSocket socket;
    private final Channel channel;
    public Client(WebSocket socket, Channel channel) {
        this.socket = socket;
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public WebSocket getSocket() {
        return socket;
    }

    public void send(String message) {
        socket.send(message);
    }
}
