package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("GET /films - получение списка всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public ResponseEntity<?> createFilm(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.info("POST /films - создание фильма: {}", film.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при создании фильма: {}", bindingResult.getFieldErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            validateFilm(film);
            film.setId(nextId++);
            films.put(film.getId(), film);

            log.info("Фильм успешно создан: {} (ID: {})", film.getName(), film.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(film);

        } catch (ValidationException e) {
            log.warn("Бизнес-ошибка при создании фильма: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании фильма: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Внутренняя ошибка сервера");
        }
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.info("PUT /films - обновление фильма ID: {}", film.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при обновлении фильма: {}", bindingResult.getFieldErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.warn("Фильм с ID {} не найден", film.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Фильм с id=" + film.getId() + " не найден");
        }

        try {
            validateFilm(film);

            films.put(film.getId(), film);

            log.info("Фильм ID {} успешно обновлен", film.getId());
            return ResponseEntity.ok(film);

        } catch (ValidationException e) {
            log.warn("Бизнес-ошибка при обновлении фильма: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении фильма: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Внутренняя ошибка сервера");
        }
    }

    private void validateFilm(Film film) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minDate)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() > 300) {
            throw new ValidationException("Продолжительность фильма не может превышать 300 минут");
        }
    }
}
