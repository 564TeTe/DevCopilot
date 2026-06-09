package com.devcopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.devcopilot")
public class DevCopilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevCopilotApplication.class, args);
    }
}
