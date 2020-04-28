package com.info7255;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.info7255.*" )

public class BigDataIndexingApplication {

    public static void main(String[] args) {

        SpringApplication.run(BigDataIndexingApplication.class, args);
    }
    }
