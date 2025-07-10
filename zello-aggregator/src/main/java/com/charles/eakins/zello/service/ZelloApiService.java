package com.charles.eakins.zello.service;

import com.charles.eakins.zello.config.ZelloApiConfig;
import com.charles.eakins.zello.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ZelloApiService {

    private static final Logger log = LoggerFactory.getLogger(ZelloApiService.class);

    private final ZelloApiConfig config;
    private final RestTemplate restTemplate;
    private final String apiHost;

    public ZelloApiService(ZelloApiConfig config, RestTemplateBuilder builder) {
        this.config = config;

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(
                List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM)
        );

        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(45))
                .build();

        this.restTemplate.getMessageConverters().add(0, converter);

        this.apiHost = "https://" + config.getNetwork() + ".zellowork.com";
    }

    // FIX: This method is for when credentials are in the properties file
    public String authenticate() {
        return authenticate(config.getUsername(), config.getPassword());
    }

    // FIX: This method is for when a user provides credentials in the login form
    public String authenticate(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            log.warn("Attempted to authenticate with empty username or password.");
            return null;
        }

        String tokenUrl = apiHost + "/user/gettoken";
        TokenResponse tokenResponse = restTemplate.getForObject(tokenUrl, TokenResponse.class);

        if (tokenResponse == null || !"OK".equals(tokenResponse.getStatus())) {
            log.error("Failed to get Zello auth token. Please check your 'zello.api.network' property.");
            if (tokenResponse != null) { log.error("Zello API status: {}", tokenResponse.getStatus()); }
            return null;
        }

        String md5Password = toMd5(password);
        String combinedAuth = md5Password + tokenResponse.getToken() + config.getKey();
        String finalAuthHash = toMd5(combinedAuth);

        String loginUrl = apiHost + "/user/login?sid=" + tokenResponse.getSid();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", finalAuthHash);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        LoginResponse loginResponse = restTemplate.postForObject(loginUrl, request, LoginResponse.class);

        if (loginResponse != null && "OK".equals(loginResponse.getStatus())) {
            log.info("Successfully authenticated user '{}'.", username);
            return tokenResponse.getSid();
        } else {
            log.error("Zello login failed for user '{}'. Status: {}, Code: {}", username, loginResponse != null ? loginResponse.getStatus() : "null", loginResponse != null ? loginResponse.getCode() : "null");
            return null;
        }
    }

    public HistoryResponse getMessages(String sid, String channel, int start, int max, Long startTs, Long endTs) {
        String url = apiHost + "/history/getmetadata?sid=" + sid;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("via_channel", channel);
        map.add("start", String.valueOf(start));
        map.add("max", String.valueOf(max));
        map.add("sort_order", "desc");

        if (startTs != null && endTs != null) {
            map.add("start_ts", String.valueOf(startTs));
            map.add("end_ts", String.valueOf(endTs));
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        return restTemplate.postForObject(url, request, HistoryResponse.class);
    }

    public Map<String, String> getUserDisplayNameMap(String sid) {
        String url = apiHost + "/user/get?sid=" + sid;
        UserListResponse response = restTemplate.getForObject(url, UserListResponse.class);
        if (response != null && "OK".equals(response.getStatus()) && response.getUsers() != null) {
            return response.getUsers().stream()
                    .collect(Collectors.toMap(
                            ZelloUser::getUsername,
                            user -> (user.getFullName() != null && !user.getFullName().isEmpty()) ? user.getFullName() : user.getUsername()
                    ));
        }
        return Collections.emptyMap();
    }

    public MediaResponse getMediaInfo(String sid, String mediaKey) {
        String url = apiHost + "/history/getmedia/key/" + mediaKey + "?sid=" + sid;
        return restTemplate.getForObject(url, MediaResponse.class);
    }

    public InputStreamResource getMediaResource(String url, String sid) throws IOException {
        String finalUrl = url + "?sid=" + sid;
        URL audioUrl = new URL(finalUrl);
        URLConnection connection = audioUrl.openConnection();
        InputStream inputStream = connection.getInputStream();
        return new InputStreamResource(inputStream);
    }

    private String toMd5(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) { hexString.append('0'); }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 Algorithm not found.", e);
            throw new RuntimeException(e);
        }
    }
}