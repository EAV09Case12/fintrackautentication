package com.example.webapi.fintrackautentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private Access access = new Access();
    private Refresh refresh = new Refresh();

    @Data
    public static class Access {
        private long expirationMs;
    }

    @Data
    public static class Refresh {
        private long expirationMs;
    }
}
