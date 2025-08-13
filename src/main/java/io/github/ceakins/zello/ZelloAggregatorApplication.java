package io.github.ceakins.zello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan("com.charles.eakins.zello.config")
public class ZelloAggregatorApplication extends SpringBootServletInitializer {

    // FIX: Define the clean file path without any prefixes.
    public static final String CONFIG_FILE_PATH = "config/application.properties";

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // Use the "optional:" prefix only here, where Spring is reading the location.
        return application.sources(ZelloAggregatorApplication.class)
                .properties("spring.config.location=optional:classpath:/application.properties,optional:" + CONFIG_FILE_PATH);
    }

    public static void main(String[] args) {
        // Use the "optional:" prefix only here, where Spring is reading the location.
        System.setProperty("spring.config.location", "optional:classpath:/application.properties,optional:" + CONFIG_FILE_PATH);
        SpringApplication.run(ZelloAggregatorApplication.class, args);
    }
}