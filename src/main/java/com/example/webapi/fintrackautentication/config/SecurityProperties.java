package com.example.webapi.fintrackautentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties {
    private int maxIntentosFallidos = 5;
}
