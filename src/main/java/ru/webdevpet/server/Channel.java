package ru.webdevpet.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;

public class Channel {
    private final String channelName;
    private final Set<Client> clientSet;
    public Channel(String channelName) {
        this.channelName = channelName;
        clientSet = new HashSet<Client>();
    }


    public void subscribe(Client client) {
        clientSet.add(client);
    }
    public void unsubscribe(Client client) {
        clientSet.remove(client);
    }
    public String getChannelName() {
        return channelName;
    }
    public void sendMessage(String message, Client currentClient) {
        for (Client client : clientSet) {
            if(!client.equals(currentClient)) {
                client.send(message);
            }

        }
    }
    public void sendMessage(String message) {
        for (Client client : clientSet) {
            client.send(message);
        }
    }
    public int getClientCount() {
        return clientSet.size();
    }

    public Client getClient(WebSocket webSocket) {
        return clientSet.stream().filter(client
                -> client.getSocket().equals(webSocket)).findFirst().orElse(null);
    }
}


