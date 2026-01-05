package ru.yandex.practicum.filmorate.controller;


import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        //validateUser(user);

        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }


    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new RuntimeException("Пользователь с id=" + user.getId() + " не найден");
        }

        //validateUser(user);

        users.put(user.getId(), user);
        return user;
    }

}
