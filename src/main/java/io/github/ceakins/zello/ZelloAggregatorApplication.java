package io.github.ceakins.zello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ConfigurationPropertiesScan("io.github.ceakins.zello.config")
public class ZelloAggregatorApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZelloAggregatorApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZelloAggregatorApplication.class, args);
    }
}