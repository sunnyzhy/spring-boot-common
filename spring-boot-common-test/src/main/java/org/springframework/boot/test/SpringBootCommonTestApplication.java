package org.springframework.boot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.log.LogbackLoggingSystemBusiness;
import org.springframework.boot.logging.LoggingSystem;

@SpringBootApplication
public class SpringBootCommonTestApplication {

    public static void main(String[] args) {
        System.setProperty("rocketmq.client.logUseSlf4j", "true");
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LogbackLoggingSystemBusiness.class.getName());
        SpringApplication.run(SpringBootCommonTestApplication.class, args);
    }

}
