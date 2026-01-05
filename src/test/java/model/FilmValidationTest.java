package model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidFilm() {
        Film film = new Film();
        film.setName("Властелин колец");
        film.setDescription("Трилогия");
        film.setReleaseDate(LocalDate.of(2001, 12, 19));
        film.setDuration(178);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "фильм не должен иметь ошибок");
    }


    @Test
    void testFilmWithNullName() {
        Film film = new Film();
        film.setName(null); // null название
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2001, 12, 19));
        film.setDuration(178);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Null должно вызывать ошибку");
    }


    @Test
    void testFilmWithNullReleaseDate() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null); // null дата
        film.setDuration(178);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Null дата должна вызывать ошибку");
    }


}
