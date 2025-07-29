package ru.yandex.practicum.core.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.clients")
public class CategoryApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryApp.class, args);
    }
}
