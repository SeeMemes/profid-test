package org.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final SpringApplication app = new SpringApplication(Main.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = app.run();

        log.debug("STARTED APPLICATION WITH BEANS: ");
        for (String beanName : applicationContext.getBeanDefinitionNames())
            log.debug("--" + beanName);
    }
}