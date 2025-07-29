package ru.yandex.practicum.core.interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.interaction.clients")
public class InteractionApi {
    public static void main(String[] args) {
        SpringApplication.run(InteractionApi.class, args);
    }
}
