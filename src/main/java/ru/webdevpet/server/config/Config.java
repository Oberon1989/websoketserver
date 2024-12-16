package ru.webdevpet.server.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private final int httpPort;
    private final int websocketPort;
    private final String token;

    @JsonCreator
    private Config(
            @JsonProperty("httpPort") int httpPort,
            @JsonProperty("websocketPort") int websocketPort,
            @JsonProperty("token") String token) {
        this.httpPort = httpPort;
        this.websocketPort = websocketPort;
        this.token = token;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public String getToken() {
        return token;
    }


    public static Config getCurrentConfig(String filePath) throws  NoSuchAlgorithmException {
        ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(filePath);

        if (Files.exists(path)) {
            try {

                String content = new String(Files.readAllBytes(path));
                Config config = mapper.readValue(content, Config.class);
                ConfigValidator.validate(config);
                return config;
            } catch (Exception ex) {
                System.out.println("Error parsing config: " + ex.getMessage());
                return null;
            }
        } else {

            Config config = new Config(8080, 8090, generateToken());

            try {
                String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
                Files.write(path, jsonStr.getBytes());
                return config;
            } catch (Exception e) {
                System.out.println("Error writing config: " + e.getMessage());
                return null;
            }
        }
    }

    private static String generateToken() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(LocalDateTime.now().toString().getBytes());
        BigInteger bigInt = new BigInteger(1, digest);
        return bigInt.toString(16);
    }
}
