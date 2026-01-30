package ru.samoylov.cloud_storage.service;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.samoylov.cloud_storage.dto.RegisterRequestDTO;
import ru.samoylov.cloud_storage.entity.User;
import ru.samoylov.cloud_storage.exception.ValidationException;
import ru.samoylov.cloud_storage.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long getCurrentUserIdByName(String name) {
        try {
            return userRepository.getUserByName(name).get().getId();
        } catch (Exception e) {
            throw new ValidationException("Пользователь не авторизован", e, HttpStatus.CONFLICT.value());
        }
    }

    public void saveUser(RegisterRequestDTO registerRequest) {
        try {
            User user = new User();
            user.setName(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            userRepository.save(user);//
        } catch (RuntimeException e) {
            throw new ValidationException("Такой пользователь уже существует", e, HttpStatus.CONFLICT.value());
        }
    }

//    public User getUserByName(String name) {
//        try {
//            return userRepository.getUserByName(name).get();
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка получения пользователя", e);
//        }
//    }
//
//    public boolean existUserByName(String name) {
//        try {
//            return userRepository.getUserByName(name).isEmpty();
//        } catch (RuntimeException e) {
//            throw new ValidationException("Такой пользователь уже существует СЕРВИС", e, HttpStatus.CONTINUE.value());
//        }
//    }


}
