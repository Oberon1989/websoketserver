package ru.webdevpet.server.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Config {
    private int httpPort;
    private int websocketPort;
    private String token;
    private String authorizeUserURL;

    @JsonCreator
    private Config(
            @JsonProperty("httpPort") int httpPort,
            @JsonProperty("websocketPort") int websocketPort,
            @JsonProperty("token") String token,
            @JsonProperty("authorizeUserURL") String authorizeUserURL) {
        this.httpPort = httpPort;
        this.websocketPort = websocketPort;
        this.token = token;
        this.authorizeUserURL = authorizeUserURL;
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

    public String getAuthorizeUserURL() {
        return authorizeUserURL;
    }

    public static Config getCurrentConfig(String filePath) throws IOException, NoSuchAlgorithmException {
        ObjectMapper mapper = new ObjectMapper();

        if (Files.exists(Paths.get(filePath))) {
            try {

                String content = new String(Files.readAllBytes(Paths.get(filePath)));


                Config config = mapper.readValue(content, Config.class);
                ConfigValidator.validate(config);
                return config;
            } catch (Exception ex) {
                System.out.println("Error parsing config112: " + ex.getMessage());
                return null;
            }
        } else {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(LocalDateTime.now().toString().getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            String token = bigInt.toString(16);

            Config config = new Config(8080, 8090, token, "");
            String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

            try {
                Files.write(Paths.get(filePath), jsonStr.getBytes());
                return config;
            } catch (Exception e) {
                System.out.println("Error writing config: " + e.getMessage());
                return null;
            }
        }
    }
}
