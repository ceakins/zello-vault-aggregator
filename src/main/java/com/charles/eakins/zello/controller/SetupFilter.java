package com.charles.eakins.zello.controller;

import com.charles.eakins.zello.ZelloAggregatorApplication;
import com.charles.eakins.zello.config.ZelloApiConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;

@Component
public class SetupFilter implements Filter {

    private final boolean isConfigured;

    public SetupFilter(ZelloApiConfig zelloApiConfig) {
        // The decision of whether the app is configured is made ONCE at startup.
        this.isConfigured = new File(ZelloAggregatorApplication.CONFIG_FILE_PATH.replace("optional:", "")).exists()
                && zelloApiConfig.getKey() != null && !zelloApiConfig.getKey().isEmpty();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);

        boolean setupCompleteButNeedsRestart = session != null && session.getAttribute("setup_complete") != null;

        // SCENARIO 1: The config has been written, but the server hasn't been restarted.
        // Trap the user on the "success" page until they restart.
        if (setupCompleteButNeedsRestart && !path.equals("/setup-success")) {
            httpResponse.sendRedirect("/setup-success");
            return;
        }

        // SCENARIO 2: The server has started, and the config file is missing.
        // Force all traffic to the setup page.
        if (!isConfigured && !path.startsWith("/setup") && !path.equals("/setup-success")) {
            httpResponse.sendRedirect("/setup");
            return;
        }

        // SCENARIO 3: The app is configured, but the user tries to access the setup page.
        // Redirect them to the main application.
        if (isConfigured && path.startsWith("/setup")) {
            httpResponse.sendRedirect("/");
            return;
        }

        // If none of the above conditions are met, proceed as normal.
        chain.doFilter(request, response);
    }
}