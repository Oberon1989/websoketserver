package ru.webdevpet.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;

public class ChannelManager {
    private final Set<Channel> channels;

    public ChannelManager() {
        channels = new HashSet<Channel>();
    }

    public Channel getChannel(String channelName) {
        Channel channel = getChannelByName(channelName);
        if (channel == null) {
            channel = new Channel(channelName);
            channels.add(channel);
            return channel;
        }
        else
            return channel;

    }

    public void removeChannel(Client client, String channelName) {
        Channel channel = getChannel(channelName);
        if (channel != null) {
            channels.remove(channel);
        }
    }

    public Channel getChannelByName(String channelName) {
        return channels.stream().filter(channel1
                -> channel1.getChannelName().equals(channelName)).findFirst().orElse(null);
    }

    public void CreateClient(WebSocket socket,String channelName) {
        Channel channel = getChannel(channelName);
        Client client = new Client(socket, channel);
        channel.subscribe(client);
    }
    public void destroyClient(WebSocket socket) {
       // Client client = channels.stream().filter(channel -> {channel.getClient()})
    }
}


