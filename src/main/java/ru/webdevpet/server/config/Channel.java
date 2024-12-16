package ru.webdevpet.server.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.java_websocket.WebSocket;
import ru.webdevpet.server.client.Client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Channel {
    private final String channelName;
    @JsonIgnore
    private final Set<Client> clientSet;
    private final boolean isPrivate;
    private final String channelPassword;
    public Channel(String channelName) {
        this.channelName = channelName;

        clientSet = new CopyOnWriteArraySet<>();
        this.isPrivate=false;
        this.channelPassword=null;
    }

    public Channel(String channelName,String channelpassword){
        clientSet = new CopyOnWriteArraySet<>();
        this.channelName=channelName;
        this.isPrivate=true;
        this.channelPassword=channelpassword;
    }

    @JsonCreator
    public Channel(
            @JsonProperty("channelName") String channelName,
            @JsonProperty("isPrivate") boolean isPrivate,
            @JsonProperty("channelPassword") String channelPassword
    ) {
        this.channelName = channelName;
        this.isPrivate = isPrivate;
        this.channelPassword = channelPassword;
        this.clientSet = new HashSet<>();
    }


    public void subscribe(Client client) throws Exception {
        if(this.isPrivate){
            String pass = client.getPassword();
            if(pass==null){
                client.close(1003,"This channel is private mode. Require password!");
                throw new Exception("password not present");
            }
            if(!pass.equals(this.channelPassword)){
                client.close(1003,"Password incorrect!");
                throw new Exception("password incorrect");
            }
        }
        clientSet.add(client);
        client.setChannel(this);

    }
    public void unsubscribe(Client client) {
        clientSet.remove(client);
    }

    public void sendMessage(String message, Client currentClient) {
        for (Client client : clientSet) {
            if(client!=null){
                if(!client.equals(currentClient)) {
                    client.send(message);
                }
            }
        }
    }
    public void sendMessage(String message) {
        for (Client client : clientSet) {
            client.send(message);
        }
    }
    @JsonIgnore
    public int getClientCount() {
        return clientSet.size();
    }

    public Client getClient(WebSocket webSocket) {
        return clientSet.stream().filter(client
                -> client.getSocket().equals(webSocket)).findFirst().orElse(null);
    }
    public void removeClients() {
        for (Client client : clientSet) {
            unsubscribe(client);
            client.setChannel(null);
            client.close(1003,"Chanel removed");
        }
    }

    @JsonProperty("channelName") // Явное указание имени свойства в JSON
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty("isPrivate")
    public boolean isPrivate() {
        return isPrivate;
    }

    @JsonProperty("channelPassword")
    public String getChannelPassword() {
        return channelPassword;
    }
}


