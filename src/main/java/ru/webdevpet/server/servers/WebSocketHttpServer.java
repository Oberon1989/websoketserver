package ru.webdevpet.server.servers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import ru.webdevpet.server.client.Client;
import ru.webdevpet.server.config.Config;
import ru.webdevpet.server.config.ConfigValidator;
import ru.webdevpet.server.dto.*;

public class WebSocketHttpServer {
    public final Config config;
    private WebSockServer webSockServer;
    private final boolean auth;
    public WebSocketHttpServer(Config config,boolean auth) throws Exception {
        this.config=config;
        this.auth=auth;
       try{
           ConfigValidator.validate(config);
       }
       catch (Exception e){
           throw new Exception("Error parse config file" + e.getMessage());
       }
    }

    public void start() throws Exception {

         webSockServer = new WebSockServer(config,this,auth);
        webSockServer.start();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
        httpServer.createContext("/send", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {

                String requestBody = readRequestBody(exchange.getRequestBody());

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Message message = objectMapper.readValue(requestBody, Message.class);
                    if (message instanceof BackendBroadcast) {
                        BackendBroadcast backendMessage = (BackendBroadcast) message;
                        webSockServer.sendMessageChanel(backendMessage.message, backendMessage.channelName);
                    }

                    else {
                        System.out.println("Unknown messageType");
                    }
                }
                catch (Exception e) {

                    System.out.println(e.getMessage());
                }


                String response = "";

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(response.getBytes());
                responseBody.close();

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        });
        httpServer.start();
        System.out.println("http server started http://localhost:"+config.getHttpPort());
    }

    private String readRequestBody(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int byteRead;
        while ((byteRead = inputStream.read()) != -1) {
            stringBuilder.append((char) byteRead);
        }
        return stringBuilder.toString();
    }
    private boolean isNullEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public void StopWebSocketServer(){
        try {
            webSockServer.stopServer();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }



}

