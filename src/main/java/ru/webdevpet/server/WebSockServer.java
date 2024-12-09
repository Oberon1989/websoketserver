package ru.webdevpet.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSockServer extends WebSocketServer {

    private final Set<Client> clients = new CopyOnWriteArraySet<>();
    public WebSockServer(int port) {
        super(new InetSocketAddress(8090)); // Передаем параметр в конструктор суперкласса

    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String uri = handshake.getResourceDescriptor();


         // Разделяем путь и параметры
        String channel = getParameter(uri, "channel");
        if(channel==null) {
            conn.send("Не указан канал, соединение будет закрыто");
            conn.close(1003);
        }
        else {
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
        Client client = getClient(conn);
        if(client!=null) {
            sendMessageChanel(client,client.getChannel(),message);
        }
    }

    private void sendMessageChanel(Client currentClient, String message, String channel) {
        clients.stream()
                .filter(client -> !client.getSocket().equals(currentClient.getSocket()) && channel.equals(client.getChannel()))  // Исключаем текущего клиента и проверяем канал
                .forEach(client -> {
                    try {
                        client.getSocket().send(message);
                    } catch (Exception e) {
                        System.out.println("Error sending message to client: " + e.getMessage());
                    }
                });
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

    private String getParameter(String uri, String key) {
        try {
            String query = uri.split("\\?")[1];

            String[] params = query.split("&");  // Разделяем параметры
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(key)) {
                    return keyValue[1];  // Возвращаем значение, если ключ совпадает
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


}