package com.charles.eakins.zello.controller;

import com.charles.eakins.zello.config.ZelloApiConfig;
import com.charles.eakins.zello.model.HistoryResponse;
import com.charles.eakins.zello.service.ZelloApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VaultController.class)
public class VaultControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ZelloApiService zelloService;

    @MockBean
    private ZelloApiConfig zelloApiConfig;

    @Test
    public void testMessageVault_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        when(zelloApiConfig.getUsername()).thenReturn("");

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMessageVault_WhenAuthenticated_ShouldShowIndex() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user_username", "testuser");
        session.setAttribute("zelloSid", "fake-sid");

        when(zelloService.getMessages(anyString(), anyString(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new HistoryResponse("OK", 0, Collections.emptyList()));
        when(zelloService.getUserDisplayNameMap(anyString()))
                .thenReturn(Map.of());

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("messages"))
                .andExpect(model().attributeExists("calendar_weeks"));
    }

    @Test
    public void testLogout_ShouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
