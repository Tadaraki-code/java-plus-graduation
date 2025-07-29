package ru.yandex.practicum.core.user.service;



import ru.yandex.practicum.core.interaction.user.dto.NewUserRequestDto;
import ru.yandex.practicum.core.interaction.user.dto.UserDto;
import ru.yandex.practicum.core.interaction.user.dto.UserShortDto;
import ru.yandex.practicum.core.interaction.user.params.UserQueryParams;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequestDto newUserRequestDto);

    List<UserDto> getAllUsers(UserQueryParams params);

    UserDto getUserById(Long userId);

    void deleteUser(Long userId);

    Boolean checkUserExisting(Long userId);

    UserShortDto getUserShortDtoById(Long userId);
}
