package ru.webdevpet.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;
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
            if(channel.getClientCount()>0){
                channel.removeClients();
            }
            channels.remove(channel);
        }
    }

    public Channel getChannelByName(String channelName) {
        return channels.stream().filter(channel1
                -> channel1.getChannelName().equals(channelName)).findFirst().orElse(null);
    }

    public void CreateClient(WebSocket socket,String channelName) {
        Channel channel = getChannel(channelName);
        Client client = new Client(socket);
        channel.subscribe(client);

    }
    public void destroyClient(WebSocket socket) {
       Client client = channels.stream()
                .map(channel -> channel.getClient(socket))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
       if(client != null) {
           Set<Channel> clientChannels = client.getChannels();
           for(Channel channel : clientChannels){
               channel.unsubscribe(client);
           }
           client.close();
       }
    }


    public void sendMessageChanel(String message, String chan){
        Channel channel = getChannel(chan);
        if(channel != null){
            channel.sendMessage(message);
        }
    }
    public void sendMessageChanel(Client currentClient, String message, String chan) {

        Channel channel = getChannel(chan);
        if(channel != null){
            channel.sendMessage(message, currentClient);
        }
    }


    public void sendErrorClient(WebSocket conn, int closeCode, String reason, boolean remote) {
            Client client = getClient(conn);
            if(client != null){
                client.send(reason);
            }
    }


    public Client getClient(WebSocket conn) {
       return channels.stream()
                .map(channel -> channel.getClient(conn))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}


