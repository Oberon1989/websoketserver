package ru.webdevpet.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import ru.webdevpet.server.config.Config;

import java.net.InetSocketAddress;


public class WebSockServer extends WebSocketServer {

    private final ChannelManager channelManager;
    private final WebSocketHttpServer http;
    private final boolean auth;
    public WebSockServer(Config config,WebSocketHttpServer http,boolean auth) {
        super(new InetSocketAddress(config.getWebsocketPort()));
        this.http=http;
        this.auth=auth;
        channelManager = new ChannelManager();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if(validateConnection(conn,handshake)){
            System.out.println("conenctiong client");
            String channel = getParameter(handshake.getResourceDescriptor(),"channel");
            channelManager.CreateClient(conn,channel);

        }


    }

    @Override
    public void onClose(WebSocket conn, int closeCode, String reason, boolean remote) {
        try{
            channelManager.sendErrorClient(conn,closeCode,reason,remote);
        }
        catch(Exception ignored){
        }

          channelManager.destroyClient(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String msg) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Message message = mapper.readValue(msg, Message.class);
            channelManager.sendMessageChanel(channelManager.getClient(conn),message.message,message.channel);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage()+"1111");
            channelManager.sendErrorClient(conn,1003,"Неверный формат сообщения! " +
                    "Пример {\"channel\":\"chanel_name\",\"message\":\"your message string\"}",false);
        }
    }

    private void sendMessageChanel(Client currentClient, String message, String channel) {
        channelManager.sendMessageChanel(currentClient,message,channel);
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

        stop();
    }



    private String getParameter(String uri, String key) {
        try {
            String query = uri.split("\\?")[1];

            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(key)) {
                    return keyValue[1];
                }
            }
            return null;
        }
        catch (Exception e) {
            return null;
        }


    }


    private boolean validateConnection(WebSocket conn,ClientHandshake handshake){
        try {
            String uri = handshake.getResourceDescriptor();
            String channel = getParameter(uri, "channel");
            String token = getParameter(uri, "token");

            if(auth){
                if(channel==null) {
                    conn.send("Missing channel name. Connection will be closed.");
                    conn.close(1003);
                    return false;
                }
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