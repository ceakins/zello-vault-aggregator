package io.github.ceakins.zello.controller;

import io.github.ceakins.zello.config.ZelloApiConfig;
import io.github.ceakins.zello.model.HistoryResponse;
import io.github.ceakins.zello.service.ConfigService;
import io.github.ceakins.zello.service.ZelloApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {VaultController.class, SetupController.class, SetupFilter.class})
public class VaultControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ZelloApiService zelloService;

    @MockBean
    private ZelloApiConfig zelloApiConfig;

    @MockBean
    private ConfigService configService;

    @Test
    public void testMessageVault_WhenNotConfigured_ShouldRedirectToSetup() throws Exception {
        // Arrange: Explicitly tell the mock that the app is NOT configured.
        when(configService.isConfigured()).thenReturn(false);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/setup"));
    }

    @Test
    public void testMessageVault_WhenConfiguredAndAuthenticated_ShouldShowIndex() throws Exception {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_username", "testuser");
        session.setAttribute("zelloSid", "fake-sid");

        // Arrange: Explicitly tell the mock that the app IS configured.
        when(configService.isConfigured()).thenReturn(true);

        when(zelloService.getMessages(anyString(), anyString(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new HistoryResponse("OK", 0, Collections.emptyList()));
        when(zelloService.getUserDisplayNameMap(anyString()))
                .thenReturn(Map.of());

        // Act & Assert
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("messages"));
    }

    @Test
    public void testLogout_ShouldInvalidateSessionAndRedirect() throws Exception {
        // FIX: Explicitly tell the mock that the app IS configured for this test case.
        when(configService.isConfigured()).thenReturn(true);

        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}