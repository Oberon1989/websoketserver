package ru.webdevpet.server.client;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import ru.webdevpet.server.config.Channel;
import ru.webdevpet.server.config.URLParser;

public class Client{
    private int clientId=0;
    private final String name;
    private final ClientHandshake handshake;
    private final WebSocket conn;
    private final String channelName;
    private final String password;
    private Channel channel;

    public Client(ClientHandshake handshake, WebSocket conn) throws Exception {
        this.handshake = handshake;
        this.conn = conn;

        try {

            this.name = URLParser.getParameter("name", this.handshake);
            if (name == null) {
                throw new Exception("Name required");
            }
            this.channelName = URLParser.getParameter("channel", this.handshake);
            if (channelName == null) {
                throw new Exception("Channel name required");
            }
            this.password = URLParser.getParameter("password", this.handshake);
            this.clientId = IDGenerator.getNextId();
        } catch (Exception e) {

            this.close(1003, "Channel name required");
            throw e;
        }
    }

    public void setChannel(Channel channel){
        this.channel=channel;
    }
    public void send(String message){
        try {
            if(conn.isOpen()){
                conn.send(message);
            }
        }
        catch (Exception e){
            System.out.println("Error sending message "+e.getMessage());
        }
    }

    public void close(int code){

        try {
            if(this.channel!=null){
                channel.unsubscribe(this);
            }
            if(this.conn.isOpen()){
                this.conn.close(code);
            }
            if(clientId>0){
                IDGenerator.releaseId(clientId);
            }

        }
        catch (Exception e){
            System.out.println("Error close client");
        }
    }

    public void close(int code,String message){

        try {
            if(this.channel!=null){
                channel.unsubscribe(this);
            }
            if(this.conn.isOpen()){
                this.conn.close(code,message);
            }
            if(clientId>0){
                IDGenerator.releaseId(clientId);
            }
        }
        catch (Exception e){
            System.out.println("Error close client");
        }
    }

    public void close(){

        try {
            if(this.channel!=null){
                channel.unsubscribe(this);
            }
            if(this.conn.isOpen()){
                this.conn.close();
            }
            if(clientId>0){
                IDGenerator.releaseId(clientId);
            }
        }
        catch (Exception e){
            System.out.println("Error close client");
        }
    }

    public String getPassword(){
        return this.password;
    }
    public String getChannelName(){
        return this.channelName;
    }

    public Channel getChannel(){
        return this.channel;
    }


    public WebSocket getSocket() {
        return this.conn;
    }

    public Integer getId() {
        return clientId;
    }
}


