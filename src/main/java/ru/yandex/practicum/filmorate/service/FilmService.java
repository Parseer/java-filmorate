package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        log.info("Добавление лайка: пользователь {} ставит лайк фильму {}", userId, filmId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));

        if (!userStorage.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Set<Integer> filmLikes = likes.computeIfAbsent(filmId, k -> new HashSet<>());

        if (filmLikes.contains(userId)) {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            return;
        }

        filmLikes.add(userId);
        log.info("Лайк добавлен. У фильма {} теперь {} лайков", filmId, filmLikes.size());
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.info("Удаление лайка: пользователь {} убирает лайк с фильма {}", userId, filmId);

        if (!filmStorage.findById(filmId).isPresent()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }

        if (!likes.containsKey(filmId) || !likes.get(filmId).contains(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не ставил лайк фильму " + filmId);
        }

        likes.get(filmId).remove(userId);
        log.info("Лайк удален. У фильма {} теперь {} лайков", filmId, likes.get(filmId).size());
    }

    public List<Film> getMostPopularFilms(Integer count) {
        log.info("Получение топ-{} популярных фильмов", count);

        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    public int getLikesCount(Integer filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).size();
    }
}