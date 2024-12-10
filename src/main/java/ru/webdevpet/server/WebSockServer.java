package ru.webdevpet.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import ru.webdevpet.server.config.Config;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSockServer extends WebSocketServer {

    private final Set<Client> clients = new CopyOnWriteArraySet<>();
    private final WebSocketHttpServer http;
    private final boolean auth;
    public WebSockServer(Config config,WebSocketHttpServer http,boolean notAuthEmail) {
        super(new InetSocketAddress(config.getWebsocketPort()));
        this.http=http;
        this.auth=notAuthEmail;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        if(validateConnection(conn,handshake)){
            System.out.println("conenctiong client");
            String channel = getParameter(handshake.getResourceDescriptor(),"channel");
            clients.add(new Client(conn, channel));
        }


    }

    @Override
    public void onClose(WebSocket conn, int closeCode, String reason, boolean remote) {
      Client client = getClient(conn);
      if(client!=null) {
          clients.remove(client);
      }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(message);
        Client client = getClient(conn);
        if(client!=null) {
            sendMessageChanel(client,message,client.getChannel());
        }
    }

    private void sendMessageChanel(Client currentClient, String message, String channel) {

        for (Client client : clients) {
            if(!client.equals(currentClient)) {
                if(client.getChannel().equals(channel)) {
                    client.send(message);
                }
            }
        }

//        clients.stream()
//                .filter(client -> !client.getSocket().equals(currentClient.getSocket()) && channel.equals(client.getChannel()))
//                .forEach(client -> {
//                    try {
//                        client.getSocket().send(message);
//                    } catch (Exception e) {
//                        System.out.println("Error sending message to client: " + e.getMessage());
//                    }
//                });
    }
    public void sendMessageChanel(String message, String channel) {

        for(Client client : clients) {
            String ch = client.getChannel();
            if(ch.equals(channel)) {
                client.getSocket().send(message);
            }
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Client client = getClient(conn);
        if(client!=null) {
            clients.remove(client);
        }
        System.out.println("Error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started successfully!");
    }


    void stopServer() throws InterruptedException {
        clients.forEach(client -> client.getSocket().close());
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

    private Client getClient(WebSocket conn) {
        return clients.stream().filter(client -> client.getSocket().equals(conn)).findFirst().orElse(null);
    }

    private boolean validateConnection(WebSocket conn,ClientHandshake handshake){
        try {
            String uri = handshake.getResourceDescriptor();
            String channel = getParameter(uri, "channel");
            String email = getParameter(uri, "email");
            String token = getParameter(uri, "token");

            if(channel==null) {
                conn.send("Missing channel name. Connection will be closed.");
                conn.close(1003);
                return false;
            }
            if(auth){
                if(!isOneServer(conn)){
                    if(email==null) {
                        conn.send("Missing email. Connection will be closed");
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


                    if(!http.sendPostRequest(email)){
                        conn.send("Failed to authorize email, Connection will be closed");
                        conn.close(1003);
                        return false;
                    }

                }
                return true;
            }
            else {
                return true;
            }



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

    public boolean isOneServer(WebSocket conn) {
        String localIp = conn.getLocalSocketAddress().getAddress().getHostAddress();
        String remoteIp = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        return localIp.equals(remoteIp);
    }


}