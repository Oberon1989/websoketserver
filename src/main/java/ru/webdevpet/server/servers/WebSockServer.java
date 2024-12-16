package ru.webdevpet.server.servers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import ru.webdevpet.server.client.Client;
import ru.webdevpet.server.dto.BroadcastMessage;
import ru.webdevpet.server.dto.Message;
import ru.webdevpet.server.config.ChannelManager;
import ru.webdevpet.server.config.Config;
import ru.webdevpet.server.config.URLParser;
import ru.webdevpet.server.dto.RequestId;
import ru.webdevpet.server.dto.UnicastMessage;

import java.net.InetSocketAddress;


public class WebSockServer extends WebSocketServer {

    private final ChannelManager channelManager;
    private final WebSocketHttpServer http;
    private final boolean auth;
    public WebSockServer(Config config,WebSocketHttpServer http,boolean auth) {
        super(new InetSocketAddress(config.getWebsocketPort()));
        this.http=http;
        this.auth=auth;
        channelManager = new ChannelManager(config);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if(validateToken(conn,handshake)){
            System.out.println("conenctiong client");
            channelManager.CreateClient(handshake,conn);
        }
        else {
            try {
                if(conn.isOpen()){
                    conn.send("Invalid token");
                    conn.close();
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }

        }


    }

    @Override
    public void onClose(WebSocket conn, int closeCode, String reason, boolean remote) {
        try{
            channelManager.destroyClient(conn);
        }
        catch(Exception e){
            System.out.println(e);
        }

    }

    @Override
    public void onMessage(WebSocket conn, String msg) {
        ObjectMapper mapper = new ObjectMapper();
        try {

            Message message = mapper.readValue(msg, Message.class);


            if (message instanceof BroadcastMessage) {
                BroadcastMessage broadcastMessage = (BroadcastMessage) message;
                channelManager.sendMessageChanel(channelManager.getClientById(broadcastMessage.idFrom), broadcastMessage.message);
            } else if (message instanceof UnicastMessage) {
                UnicastMessage unicastMessage = (UnicastMessage) message;

                channelManager.sendMessageById(unicastMessage.idTo, unicastMessage.message);
            }
            else if(message instanceof RequestId){
                Client client = channelManager.getClientBySocket(conn);
                if(client!=null){
                    client.send(client.getId()+"");
                }
            }
            else {
                System.out.println("Неизвестный тип сообщения.");
            }

        } catch (JsonProcessingException e) {
            System.out.println("Ошибка при парсинге JSON: " + e.getMessage());
            channelManager.getClient(conn).send("Неверный формат сообщения!");
        }
    }


    public void sendMessageChanel(String message, String channel) {
        channelManager.sendMessageChanel(message,channel);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
            channelManager.destroyClient(conn);
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started in ws://localhost:" + getPort());
    }


    void stopServer() throws InterruptedException {
        channelManager.destroyClients();
        stop();
    }



    private boolean validateToken(WebSocket conn,ClientHandshake handshake){
        try {
            String token = URLParser.getParameter( "token",handshake);

            if(auth){

                if(token==null) {
                    conn.send("Missing token. Connection will be closed");
                    conn.close(1003);
                    return false;
                }
                if(!token.equals(http.config.getToken())){
                    conn.send("Invalid token. Connection will be closed");
                    conn.close(1003);
                    return false;
                }
                return true;
            }
            return true;
        }
        catch (Exception ex){
            if(conn.isOpen()){
                conn.send("Internal server error, Connection will be closed");
                conn.close(1003);
                return false;
            }
            return false;
        }

    }

}