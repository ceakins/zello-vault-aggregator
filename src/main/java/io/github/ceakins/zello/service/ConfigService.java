package io.github.ceakins.zello.service;

import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    private static final String CONFIG_DIR_NAME = "config";
    private static final String CONFIG_FILE_NAME = "application.properties";

    private final File configFile;
    private boolean isConfigured = false;

    public ConfigService(ServletContext servletContext, ConfigurableEnvironment environment) {
        String appRootPath;
        String realPath = servletContext.getRealPath("/");

        // FIX: Detect if we are running in an unpacked temporary directory from an IDE.
        boolean isUnpackedWar = realPath != null && (realPath.contains("tomcat-docbase") || realPath.contains("jetty-"));

        if (isUnpackedWar) {
            // If running from an IDE's temp folder, use a stable path in the project's root directory.
            appRootPath = new File(".").getAbsolutePath();
            log.warn("IDE's temporary deployment directory detected. Using stable project root for configuration: {}", appRootPath);
        } else if (realPath != null) {
            // This is for a real Tomcat server deployment.
            appRootPath = realPath;
        } else {
            // This is the fallback for a standalone JAR execution.
            appRootPath = new File(".").getAbsolutePath();
        }

        // The path to the config file is now stable for local development and correct for production.
        // For local runs, it will be C:\dev\projects\zello-aggregator\config\application.properties
        // For Tomcat, it will be /opt/tomcat/webapps/zello-vault-aggregator/WEB-INF/config/application.properties
        String configDirectoryPath = isUnpackedWar ?
                appRootPath + File.separator + CONFIG_DIR_NAME :
                appRootPath + File.separator + "WEB-INF" + File.separator + CONFIG_DIR_NAME;

        this.configFile = new File(configDirectoryPath + File.separator + CONFIG_FILE_NAME);

        if (this.configFile.exists() && this.configFile.length() > 0) {
            log.info("Found external configuration file at: {}", this.configFile.getAbsolutePath());
            try {
                Properties properties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(this.configFile));
                PropertiesPropertySource propertySource = new PropertiesPropertySource("externalConfig", properties);
                environment.getPropertySources().addFirst(propertySource);
                this.isConfigured = true;
                log.info("Successfully loaded and applied external configuration.");
            } catch (IOException e) {
                log.error("Could not load external configuration file from {}", this.configFile.getAbsolutePath(), e);
            }
        } else {
            log.warn("External configuration file not found at: {}. Application is not configured.", this.configFile.getAbsolutePath());
        }
    }

    public boolean isConfigured() {
        return this.isConfigured;
    }

    public void saveConfiguration(Map<String, String> properties) throws IOException {
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            if (!configDir.mkdirs()) {
                throw new IOException("Could not create config directory at: " + configDir.getAbsolutePath());
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
            log.info("Successfully wrote configuration to {}", configFile.getAbsolutePath());
        }
    }

    public String getConfigFilePath() {
        return configFile.getAbsolutePath();
    }
}