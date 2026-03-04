package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("GET /films - получение всех фильмов");
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable Integer id) {
        log.info("GET /films/{} - получение фильма по ID", id);

        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));

        return ResponseEntity.ok(film);
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
            Film createdFilm = filmStorage.create(film);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);

        } catch (ValidationException e) {
            log.warn("Ошибка при создании фильма: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.info("PUT /films - обновление фильма ID {}", film.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при обновлении фильма: {}", bindingResult.getFieldErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Optional<Film> updatedFilm = filmStorage.update(film);
            if (updatedFilm.isPresent()) {
                log.info("Фильм ID {} обновлен", film.getId());
                return ResponseEntity.ok(updatedFilm.get());
            } else {
                log.warn("Фильм с ID {} не найден", film.getId());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Фильм с id=" + film.getId() + " не найден");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (ValidationException e) {
            log.warn("Ошибка при обновлении фильма: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении фильма: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Внутренняя ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /films/{}/like/{} - добавление лайка", id, userId);

        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /films/{}/like/{} - удаление лайка", id, userId);

        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("GET /films/popular?count={} - получение популярных фильмов", count);
        return filmService.getMostPopularFilms(count);
    }

}