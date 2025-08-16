package io.github.ceakins.zello.controller;

import io.github.ceakins.zello.service.ConfigService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SetupFilter implements Filter {

    private final ConfigService configService;

    public SetupFilter(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // FIX: The logic is now much cleaner. It just asks the ConfigService.
        if (!configService.isConfigured() && !path.startsWith(contextPath + "/setup")) {
            String redirectUrl = contextPath + "/setup";
            httpResponse.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(request, response);
    }
}