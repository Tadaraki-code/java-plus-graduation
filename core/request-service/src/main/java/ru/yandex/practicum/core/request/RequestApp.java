package ru.yandex.practicum.core.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.request",
        "ru.practicum.ewm"
})
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.clients")
public class RequestApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestApp.class, args);
    }
}
