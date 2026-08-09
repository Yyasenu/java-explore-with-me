package ru.practicum.ewm.user;

import java.util.List;

public interface UserService {

    public UserResponseDto getUserById(Long userId);

    public UserResponseDto addUserAdmin(NewUserDto userDto);

    List<UserResponseDto> getUsersAdmin(List<Long> ids, Integer from, Integer size);

    void deleteUserAdmin(Long userId);
}
