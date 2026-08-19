package ru.practicum.ewm.user;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

    public User toUser(NewUserDto userDto) {
        return User.builder()
                .id(null)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
