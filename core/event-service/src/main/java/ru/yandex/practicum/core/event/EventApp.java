package ru.yandex.practicum.core.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.event",
        "ru.practicum.ewm.client"
})
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.clients")
public class EventApp {
    public static void main(String[] args) {
        SpringApplication.run(EventApp.class, args);
    }
}
