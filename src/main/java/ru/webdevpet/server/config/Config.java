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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private final int httpPort;
    private final int websocketPort;
    private final String token;
    private final List<Channel> channels;
    @JsonCreator
    private Config(
            @JsonProperty("httpPort") int httpPort,
            @JsonProperty("websocketPort") int websocketPort,
            @JsonProperty("token") String token,@JsonProperty("channels") List<Channel> channels){
        this.httpPort = httpPort;
        this.websocketPort = websocketPort;
        this.token = token;
        this.channels = channels;
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
    public List<Channel> getChannels(){
        return channels;
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


            Channel server = new Channel("server","123456");
            Channel chat = new Channel("chat","123456");
            List<Channel> channels = Stream.of(server,chat).collect(Collectors.toList());
            Config config = new Config(8080, 8090, generateToken(),channels);

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
        StringBuilder token = new StringBuilder(new BigInteger(1, digest).toString(16));

        while (token.length() < 32) {
            token.insert(0, "0");
        }

        return token.toString();
    }
}
