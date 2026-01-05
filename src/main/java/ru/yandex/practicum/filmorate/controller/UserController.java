package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET /users - получение всех пользователей");
        List<User> userList = new ArrayList<>(users.values());
        userList.forEach(user -> {
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(user.getLogin());
            }
        });
        return userList;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.info("POST /users - создание пользователя: {}", user.getLogin());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при создании пользователя: {}", bindingResult.getFieldErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            validateUser(user);

            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(user.getLogin());
            }

            user.setId(nextId++);
            users.put(user.getId(), user);

            log.info("Пользователь '{}' создан с ID {}", user.getLogin(), user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (ValidationException e) {
            log.warn("Бизнес-ошибка при создании пользователя: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании пользователя: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Внутренняя ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.info("PUT /users - обновление пользователя ID {}", user.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", bindingResult.getFieldErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.warn("Пользователь с ID {} не найден", user.getId());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Пользователь с id=" + user.getId() + " не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        try {
            validateUser(user);
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Пользователь ID {} обновлен", user.getId());
            
            return ResponseEntity.ok(user);

        } catch (ValidationException e) {
            log.warn("Ошибка при обновлении пользователя: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении пользователя: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Внутренняя ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private void validateUser(User user) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()) && !u.getId().equals(user.getId()));
        if (emailExists) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        boolean loginExists = users.values().stream()
                .anyMatch(u -> u.getLogin().equals(user.getLogin()) && !u.getId().equals(user.getId()));
        if (loginExists) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }
    }
}