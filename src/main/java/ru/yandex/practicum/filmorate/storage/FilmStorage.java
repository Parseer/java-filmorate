package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Optional<Film> update(Film film);

    Optional<Film> findById(Integer id);

    List<Film> findAll();

    void delete(Integer id);
}
