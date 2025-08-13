package com.charles.eakins.zello.controller;

import com.charles.eakins.zello.config.ZelloApiConfig;
import com.charles.eakins.zello.model.HistoryResponse;
import com.charles.eakins.zello.service.ZelloApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// We test the controllers we need.
// We explicitly EXCLUDE the SetupFilter from this test's application context.
@WebMvcTest(
        controllers = {VaultController.class, SetupController.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SetupFilter.class)
)
// We provide all necessary properties so the @Value fields in the controller are satisfied.
@TestPropertySource(properties = {
        "zello.api.network=test-network",
        "zello.api.key=test-key",
        "app.target-channel=Emergency Communications",
        "app.messages-per-page=50",
        "app.display-timezone=America/Los_Angeles",
        "app.session-timeout-seconds=1800",
        "app.session-warning-seconds=15",
        "app.auto-refresh-seconds=60"
})
public class VaultControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    // These beans are required by the controllers we are testing.
    // Spring will provide mock implementations.
    @MockBean
    private ZelloApiService zelloService;

    @MockBean
    private ZelloApiConfig zelloApiConfig;

    @Test
    public void testMessageVault_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        // Arrange: Simulate that hardcoded credentials are not present.
        when(zelloApiConfig.getUsername()).thenReturn("");

        // Act & Assert
        // This request now goes directly to VaultController because the filter has been excluded.
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMessageVault_WhenAuthenticated_ShouldShowIndex() throws Exception {
        // Arrange
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_username", "testuser");
        session.setAttribute("zelloSid", "fake-sid");

        when(zelloService.getMessages(anyString(), anyString(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new HistoryResponse("OK", 0, Collections.emptyList()));
        when(zelloService.getUserDisplayNameMap(anyString()))
                .thenReturn(Map.of());

        // Act & Assert
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    public void testLogout_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}