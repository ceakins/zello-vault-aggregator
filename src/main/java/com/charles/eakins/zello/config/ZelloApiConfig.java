package com.charles.eakins.zello.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zello.api")
public class ZelloApiConfig {
    private final String network;
    private final String username;
    private final String password;
    private final String key;

    public ZelloApiConfig(String network, String username, String password, String key) {
        this.network = network;
        this.username = username;
        this.password = password;
        this.key = key;
    }

    public String getNetwork() { return network; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getKey() { return key; }
}