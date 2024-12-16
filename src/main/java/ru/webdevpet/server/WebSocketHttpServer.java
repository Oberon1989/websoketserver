package ru.webdevpet.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import ru.webdevpet.server.config.Config;
import ru.webdevpet.server.config.ConfigValidator;

public class WebSocketHttpServer {
    public Config config;
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

        WebSockServer webSockServer = new WebSockServer(config,this,auth);
        webSockServer.start();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.getHttpPort()), 0);
        httpServer.createContext("/send", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Чтение тела запроса
                String requestBody = readRequestBody(exchange.getRequestBody());

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    HttpMessage httpMessage = objectMapper.readValue(requestBody, HttpMessage.class);
                    if(!isNullEmpty(httpMessage.channel) && !isNullEmpty(httpMessage.message)){
                        webSockServer.sendMessageChanel(httpMessage.message,httpMessage.channel);
                    }
                }
                catch (Exception e) {
                    try {
                        webSockServer.stopServer();
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

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



}

