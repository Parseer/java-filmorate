package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film create(Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм '{}' создан с ID {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        validateFilm(film);
        if (film.getId() != null && films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм ID {} обновлен", film.getId());
            return Optional.of(film);
        }
        log.warn("Фильм с ID {} не найден для обновления", film.getId());
        return Optional.empty();
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(Integer id) {
        films.remove(id);
        log.info("Фильм с ID {} удален", id);
    }

    private void validateFilm(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(minDate)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}