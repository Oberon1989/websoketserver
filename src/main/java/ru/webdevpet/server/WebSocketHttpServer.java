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
        httpServer.createContext("/send", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
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



                    // Подготовка ответа
                    String response = "";

                    // Отправка ответа клиенту
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(response.getBytes());
                    responseBody.close();

                } else {
                    exchange.sendResponseHeaders(405, -1); // Метод не поддерживается
                }
                exchange.close();
            }
        });
        httpServer.start();
        System.out.println("http server started http://localhost:8080");
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

    public  boolean sendPostRequest(String email) {
        HttpURLConnection connection = null;

        try {
            connection.setReadTimeout(1);
            URL url = new URL(config.getAuthorizeUserURL()+"?email="+email);
            connection.setConnectTimeout(5000); // Таймаут на подключение (5 секунд)
            connection.setReadTimeout(5000);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/text; utf-8");
            connection.setRequestProperty("Accept", "application/text");



            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}

