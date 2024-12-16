package ru.webdevpet.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;

public class Client {
    private final WebSocket socket;
    private final Set<Channel> channels;
    public Client(WebSocket socket) {
        this.socket = socket;
        this.channels = new HashSet<>();
    }

    public Set<Channel> getChannels() {
        return channels;
    }

    public WebSocket getSocket() {
        return socket;
    }

    public void putChannel(Channel channel) {
        channels.add(channel);
    }
    public void removeChannel(Channel channel) {
        channels.remove(channel);
    }

    public void send(String message) {
        socket.send(message);
    }

    public void close() {
        try {
            for (Channel channel : channels) {
                channel.unsubscribe(this);
            }
            channels.clear();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
