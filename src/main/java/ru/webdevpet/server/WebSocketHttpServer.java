package ru.webdevpet.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.fasterxml.jackson.*;
public class WebSocketHttpServer {



    public  void start() throws Exception {

        WebSockServer s = new WebSockServer(8090);
        s.start();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
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
                            s.sendMessageChanel(httpMessage.message,httpMessage.channel);
                        }
                    }
                    catch (Exception e) {
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

}

