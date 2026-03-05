package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        validateUser(user);

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь '{}' создан с ID {}", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            return Optional.empty();
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }
        validateUser(user);

        users.put(user.getId(), user);
        log.info("Пользователь ID {} обновлен", user.getId());
        return Optional.of(user);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>(users.values());
        userList.forEach(user -> {
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                user.setName(user.getLogin());
            }
        });
        return userList;
    }

    @Override
    public void delete(Integer id) {
        users.remove(id);
        log.info("Пользователь с ID {} удален", id);
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