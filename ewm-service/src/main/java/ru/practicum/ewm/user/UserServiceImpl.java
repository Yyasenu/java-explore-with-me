package ru.practicum.ewm.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toResponseDto)
                .orElseThrow(() -> new NotFoundException("User was not found: " + userId));
    }

    @Override
    @Transactional
    public UserResponseDto addUserAdmin(NewUserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toResponseDto(savedUser);
    }

    @Override
    public List<UserResponseDto> getUsersAdmin(List<Long> ids, Integer from, Integer size) {
        Page<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findByIdIn(ids, PageRequest.of(from / size, size));
        } else {
            users = userRepository.findAll(PageRequest.of(from / size, size));
        }

        return users.stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUserAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with was not found " + userId));
        userRepository.delete(user);
    }
}
