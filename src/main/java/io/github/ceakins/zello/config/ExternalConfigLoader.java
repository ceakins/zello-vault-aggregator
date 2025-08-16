package io.github.ceakins.zello.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ExternalConfigLoader implements ApplicationListener<ApplicationPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ExternalConfigLoader.class);
    // This is the external location, relative to where the app is run or deployed
    private static final String CONFIG_FILE_PATH = "config/application.properties";

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        File configFile = new File(CONFIG_FILE_PATH);

        if (configFile.exists()) {
            log.info("Found external configuration file at: {}", configFile.getAbsolutePath());
            try {
                Properties properties = PropertiesLoaderUtils.loadProperties(new FileSystemResource(configFile));
                PropertiesPropertySource propertySource = new PropertiesPropertySource("externalConfig", properties);
                // Add our external properties with the highest priority
                environment.getPropertySources().addFirst(propertySource);
                log.info("Successfully loaded and applied external configuration.");
            } catch (IOException e) {
                throw new IllegalStateException("Could not load external configuration file", e);
            }
        } else {
            log.warn("External configuration file not found at: {}. Application will start in setup mode.", configFile.getAbsolutePath());
        }
    }
}