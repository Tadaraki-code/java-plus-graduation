package ru.yandex.practicum.core.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.clients")
public class CompilationApp {
    public static void main(String[] args) {
        SpringApplication.run(CompilationApp.class, args);
    }
}
