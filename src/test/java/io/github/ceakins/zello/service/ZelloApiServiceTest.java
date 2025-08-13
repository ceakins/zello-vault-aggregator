package io.github.ceakins.zello.service;

import io.github.ceakins.zello.config.ZelloApiConfig;
import io.github.ceakins.zello.model.LoginResponse;
import io.github.ceakins.zello.model.TokenResponse;
import io.github.ceakins.zello.model.UserListResponse;
import io.github.ceakins.zello.model.ZelloUser;
// REMOVED: import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ZelloApiServiceTest {

    // --- Mocks are still needed for the dependencies ---
    @Mock
    private ZelloApiConfig mockConfig;

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private RestTemplateBuilder mockBuilder;

    // --- The service instance we are testing ---
    private ZelloApiService zelloApiService; // FIX: REMOVED @InjectMocks

    @BeforeMethod
    public void setUp() {
        // Initializes all the @Mock fields above
        MockitoAnnotations.openMocks(this);

        // Step 1: Configure all our mocks BEFORE creating the service instance
        when(mockBuilder.setConnectTimeout(any())).thenReturn(mockBuilder);
        when(mockBuilder.setReadTimeout(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRestTemplate);

        // Step 2: Manually create the service instance, passing in our fully configured mocks
        zelloApiService = new ZelloApiService(mockConfig, mockBuilder);
    }

    @Test
    public void testAuthenticate_Success() {
        when(mockConfig.getNetwork()).thenReturn("test-network");
        when(mockConfig.getUsername()).thenReturn("testuser");
        when(mockConfig.getPassword()).thenReturn("password123");
        when(mockConfig.getKey()).thenReturn("test-api-key");

        TokenResponse tokenResponse = new TokenResponse("OK", "test-token", "test-sid");
        LoginResponse loginResponse = new LoginResponse("OK", "200");

        when(mockRestTemplate.getForObject(anyString(), eq(TokenResponse.class))).thenReturn(tokenResponse);
        when(mockRestTemplate.postForObject(anyString(), any(HttpEntity.class), eq(LoginResponse.class))).thenReturn(loginResponse);

        String resultSid = zelloApiService.authenticate();

        Assert.assertEquals(resultSid, "test-sid");
    }

    @Test
    public void testGetUserDisplayNameMap_Success() {
        List<ZelloUser> users = List.of(
                new ZelloUser("user1", "Charles Eakins"),
                new ZelloUser("user2", "Another User"),
                new ZelloUser("user3", "")
        );
        UserListResponse userListResponse = new UserListResponse("OK", users);

        when(mockRestTemplate.getForObject(anyString(), eq(UserListResponse.class))).thenReturn(userListResponse);

        Map<String, String> userMap = zelloApiService.getUserDisplayNameMap("any-sid");

        Assert.assertEquals(userMap.size(), 3);
        Assert.assertEquals(userMap.get("user1"), "Charles Eakins");
        Assert.assertEquals(userMap.get("user2"), "Another User");
        Assert.assertEquals(userMap.get("user3"), "user3");
    }
}