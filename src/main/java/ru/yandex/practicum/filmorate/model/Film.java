package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.*; // доп пакет

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    public boolean isReleaseDateValid() {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        return releaseDate != null && !releaseDate.isBefore(minDate);
    }

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

}
