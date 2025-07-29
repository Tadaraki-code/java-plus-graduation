package ru.yandex.practicum.core.interaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;

@FeignClient(name = "compilation-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface CompilationClient {
}
