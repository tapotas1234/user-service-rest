package org.tapotas.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tapotas.dto.CreateUserDto;
import org.tapotas.dto.UserDto;
import org.tapotas.entities.UserEntity;
import org.tapotas.model.EmailNotificationMessage;
import org.tapotas.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${kafka.topic.email-notifications}")
    private String TOPIC;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        if (userRepository.existsByName(createUserDto.getName())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        EmailNotificationMessage message = new EmailNotificationMessage(
                createUserDto.getEmail(),
                "Ваш аккаунт успешно создан",
                "Сообщение о создании аккаунта");

        kafkaTemplate.send(TOPIC, message);

        UserEntity userEntity = modelMapper.map(createUserDto, UserEntity.class);
        UserEntity savedUserEntity = userRepository.save(userEntity);
        return modelMapper.map(savedUserEntity, UserDto.class);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(userEntity, UserDto.class);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        UserEntity existingUserEntity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingUserEntity.getName().equals(userDto.getName())) {
            if (userRepository.existsByName(userDto.getName())) {
                throw new RuntimeException("Username already exists");
            }
        }

        modelMapper.map(userDto, existingUserEntity);
        UserEntity updatedUserEntity = userRepository.save(existingUserEntity);
        return modelMapper.map(updatedUserEntity, UserDto.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        UserDto user = getUserById(id);

        EmailNotificationMessage message = new EmailNotificationMessage(
                user.getEmail(),
                "Ваш аккаунт удален",
                "Сообщение об удалении аккаунта");
        kafkaTemplate.send(TOPIC, message);

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDto.class))
                .collect(Collectors.toList());
    }
}