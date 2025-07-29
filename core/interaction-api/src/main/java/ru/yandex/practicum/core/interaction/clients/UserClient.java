package ru.yandex.practicum.core.interaction.clients;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;
import ru.yandex.practicum.core.interaction.user.dto.UserDto;
import ru.yandex.practicum.core.interaction.user.dto.UserShortDto;


import static ru.yandex.practicum.core.interaction.user.constants.UserConstants.*;

@FeignClient(name = "user-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface UserClient {

    @GetMapping(INTERACTION_API_PREFIX + USER_ID_PATH)
    UserDto getUserById(@Valid @PathVariable(USER_ID) Long userId);

    @GetMapping(INTERACTION_API_PREFIX + INTERACTION_EXISTING_API_PREFIX + USER_ID_PATH)
    Boolean checkUserExisting(@Valid @PathVariable(USER_ID) Long userId);

    @GetMapping(INTERACTION_API_PREFIX + SHORT_API_PREFIX + USER_ID_PATH)
    UserShortDto getUserShortDtoById(@Valid @PathVariable(USER_ID) Long userId);
}
