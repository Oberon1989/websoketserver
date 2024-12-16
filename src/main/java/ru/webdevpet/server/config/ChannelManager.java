package ru.webdevpet.server.config;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import ru.webdevpet.server.client.Client;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChannelManager {
    private final Set<Channel> channels;
    private final ConcurrentHashMap<Integer, Client> clients;

    public ChannelManager(Config config) {
        channels = new CopyOnWriteArraySet<>();
        channels.addAll(config.getChannels());
        clients = new ConcurrentHashMap<>();
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

    public Channel getChannelByName(String channelName) {
        return channels.stream().filter(channel1
                -> channel1.getChannelName().equals(channelName)).findFirst().orElse(null);
    }

    public void CreateClient(ClientHandshake handshake,WebSocket socket) {

      try{
          Client client = new Client(handshake,socket);
          Channel channel = getChannel(client.getChannelName());
          channel.subscribe(client);
          addClient(client);
      }
      catch (Exception e){
          System.out.println(e.getMessage());
      }


    }
    public void destroyClient(WebSocket socket) {
       Client client = getClientBySocket(socket);
       if(client != null) {
           client.getChannel().unsubscribe(client);
           client.close(1003,"close");
           removeClientById(client.getId());
       }
    }


    public void sendMessageChanel(String message, String chan){
        Channel channel = getChannel(chan);
        if(channel != null){
            channel.sendMessage(message);
        }
    }
    public void sendMessageChanel(Client currentClient, String message) {

        Channel channel =currentClient.getChannel();
        if(channel != null){
            channel.sendMessage(message, currentClient);
        }
    }

    public void sendMessageById(int id, String message){
        try {
            Client client = getClientById(id);
            if(client!=null){
                client.send(message);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }


    public Client getClient(WebSocket conn) {
       return channels.stream()
                .map(channel -> channel.getClient(conn))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public void destroyClients() {
        this.channels.forEach(Channel::removeClients);
    }

    public Client getClientById(int id) {
        return clients.get(id);
    }

    public void addClient(Client client) {
        clients.put(client.getId(), client);
    }
    public void removeClientById(int id) {
        clients.remove(id);
    }
    public void clearAllClients() {
        clients.clear();
    }

    public Client getClientBySocket(WebSocket sock){
        for (Client client : clients.values()) {
            if (client.getSocket().equals(sock)) {  // Сравниваем по WebSocket
                return client;
            }
        }
        return null;
    }
}


