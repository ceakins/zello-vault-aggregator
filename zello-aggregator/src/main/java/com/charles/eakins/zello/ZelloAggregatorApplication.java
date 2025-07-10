package com.charles.eakins.zello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder; // <-- ADDED
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // <-- ADDED

@SpringBootApplication
@ConfigurationPropertiesScan("com.charles.eakins.zello.config")
// STEP 4: Extend SpringBootServletInitializer
public class ZelloAggregatorApplication extends SpringBootServletInitializer {

    // STEP 5: Override the configure method
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZelloAggregatorApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZelloAggregatorApplication.class, args);
    }

}