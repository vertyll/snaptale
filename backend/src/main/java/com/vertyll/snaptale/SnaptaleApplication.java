package com.vertyll.snaptale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SnaptaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnaptaleApplication.class, args);
    }
}
