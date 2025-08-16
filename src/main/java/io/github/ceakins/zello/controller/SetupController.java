package io.github.ceakins.zello.controller;

import io.github.ceakins.zello.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Controller
public class SetupController {

    private static final Logger log = LoggerFactory.getLogger(SetupController.class);
    private final ConfigService configService;

    public SetupController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/setup")
    public String setupPage(Model model) {
        if (configService.isConfigured()) {
            return "redirect:/";
        }

        try {
            // FIX: This path now correctly finds the file inside src/main/resources/config/
            Properties defaultProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("config/application.properties.template"));
            model.addAttribute("defaults", defaultProps);
        } catch (IOException e) {
            log.error("Could not load config/application.properties.template from classpath.", e);
            model.addAttribute("defaults", new Properties());
        }

        return "setup";
    }

    @PostMapping("/setup")
    public String handleSetup(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        Map<String, String> propertiesToSave = allParams.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("_csrf"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        try {
            configService.saveConfiguration(propertiesToSave);
            redirectAttributes.addFlashAttribute("configFilePath", configService.getConfigFilePath());
            return "redirect:/setup/success";
        } catch (IOException e) {
            log.error("Error saving configuration file", e);
            redirectAttributes.addFlashAttribute("error", "Error saving configuration: " + e.getMessage());
            return "redirect:/setup";
        }
    }

    @GetMapping("/setup/success")
    public String setupSuccessPage(Model model) {
        if (!model.containsAttribute("configFilePath")) {
            // This prevents users from accessing the page directly.
            return "redirect:/";
        }
        return "setup-success";
    }
}