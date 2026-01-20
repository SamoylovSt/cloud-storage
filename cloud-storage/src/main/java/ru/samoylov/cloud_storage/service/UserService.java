package ru.samoylov.cloud_storage.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.samoylov.cloud_storage.entity.User;
import ru.samoylov.cloud_storage.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getCurrentUserIdByName(String name) {
        try {
            return userRepository.getUserByName(name).get().getId();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения пользователя", e);
        }
    }

    public User getUserByName(String name) {
        try {
            return userRepository.getUserByName(name).get();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения пользователя", e);
        }
    }
}
