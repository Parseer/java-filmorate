package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.info("Добавление друга: пользователь {} добавляет {}", userId, friendId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Удаление друга: пользователь {} удаляет {}", userId, friendId);

        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + friendId + " не найден"));

        if (!friends.containsKey(userId) || !friends.get(userId).contains(friendId)) {
            log.warn("Пользователи {} и {} не являются друзьями, удаление не требуется", userId, friendId);
            return;
        }

        friends.get(userId).remove(friendId);
        friends.get(friendId).remove(userId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getUserFriends(Integer userId) {
        log.info("Получение списка друзей пользователя {}", userId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        return friends.getOrDefault(userId, Collections.emptySet()).stream()
                .map(friendId -> userStorage.findById(friendId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userStorage.findById(otherId).isPresent()) {
            throw new NotFoundException("Пользователь с id=" + otherId + " не найден");
        }

        Set<Integer> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Integer> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(friendId -> userStorage.findById(friendId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}