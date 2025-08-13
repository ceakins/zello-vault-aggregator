package io.github.ceakins.zello.controller;

import io.github.ceakins.zello.ZelloAggregatorApplication;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

@Controller
public class SetupController {

    private static final Logger log = LoggerFactory.getLogger(SetupController.class);

    @GetMapping("/setup")
    public String setupForm(Model model) {
        model.addAttribute("defaultPort", 8080);
        model.addAttribute("defaultChannel", "Emergency Communications");
        model.addAttribute("defaultMessagesPerPage", 50);
        model.addAttribute("defaultTimezone", "America/Los_Angeles");
        model.addAttribute("defaultSessionTimeout", 1800);
        model.addAttribute("defaultSessionWarning", 15);
        model.addAttribute("defaultAutoRefresh", 60);
        return "setup";
    }

    @PostMapping("/setup")
    public String saveSetup(
            @RequestParam String zelloNetwork, @RequestParam String zelloApiKey,
            @RequestParam String appChannel, @RequestParam int serverPort,
            @RequestParam String appTimezone, @RequestParam int appMessagesPerPage,
            @RequestParam int appSessionTimeout, @RequestParam int appSessionWarning,
            @RequestParam int appAutoRefresh, HttpSession session) {

        Properties props = new Properties();
        props.setProperty("zello.api.network", zelloNetwork);
        props.setProperty("zello.api.key", zelloApiKey);
        props.setProperty("zello.api.username", "");
        props.setProperty("zello.api.password", "");
        props.setProperty("app.target-channel", appChannel);
        props.setProperty("app.messages-per-page", String.valueOf(appMessagesPerPage));
        props.setProperty("app.display-timezone", appTimezone);
        props.setProperty("app.session-timeout-seconds", String.valueOf(appSessionTimeout));
        props.setProperty("app.session-warning-seconds", String.valueOf(appSessionWarning));
        props.setProperty("app.auto-refresh-seconds", String.valueOf(appAutoRefresh));
        props.setProperty("server.port", String.valueOf(serverPort));
        props.setProperty("management.endpoints.web.exposure.include", "refresh");

        File configFile = new File(ZelloAggregatorApplication.CONFIG_FILE_PATH.replace("optional:", ""));
        File configDir = configFile.getParentFile();

        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                log.error("Failed to create config directory at: {}", configDir.getAbsolutePath());
                return "error";
            }
            log.info("Created config directory at: {}", configDir.getAbsolutePath());
        }

        try (OutputStream output = new FileOutputStream(configFile)) {
            props.store(output, "Zello Vault Aggregator Configuration");
            log.info("Successfully saved configuration to {}", configFile.getAbsolutePath());
            // Set the session flag to trap the user on the success page
            session.setAttribute("setup_complete", true);
            return "redirect:/setup-success";
        } catch (IOException e) {
            log.error("Failed to write configuration file", e);
            return "error";
        }
    }

    @GetMapping("/setup-success")
    public String setupSuccess() {
        return "setup-success";
    }
}