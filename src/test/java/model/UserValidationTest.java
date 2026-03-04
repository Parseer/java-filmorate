package model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user123");
        user.setName("Иван Иванов");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "пользователь не должен иметь ошибок");
    }

    @Test
    void testUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email"); // без @
        user.setLogin("user123");
        user.setName("Иван Иванов");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "email должен вызывать ошибку");

    }

    @Test
    void testUserWithEmptyEmail() {
        User user = new User();
        user.setEmail(""); // email
        user.setLogin("user123");
        user.setName("Иван Иванов");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "email должен вызывать ошибку");
    }

    @Test
    void testUserWithSpacesInLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user 123"); // Логин с пробелом
        user.setName("Иван Иванов");
        user.setBirthday(LocalDate.of(1990, 5, 15));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Логин должен вызывать ошибку");
    }

}
