package ru.yandex.practicum.core.interaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.config.FeignConfig;
import ru.yandex.practicum.core.interaction.decoders.CommonFeignErrorDecoder;

import java.util.List;

import static ru.yandex.practicum.core.interaction.category.constants.CategoryConstants.*;


@FeignClient(name = "category-service", configuration = {FeignConfig.class, CommonFeignErrorDecoder.class})
public interface CategoryClient {


    @GetMapping(PUBLIC_API_PREFIX + CAT_ID_PATH)
    CategoryDto getCategoryById(@PathVariable(CAT_ID) Long catId);

    @GetMapping(INTERACTION_API_PREFIX)
    List<CategoryDto> getCategoryByIds(@RequestParam("categoryIds") List<Long> categoryIds);
}
