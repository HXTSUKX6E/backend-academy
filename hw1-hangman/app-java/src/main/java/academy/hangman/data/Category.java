package academy.hangman.data;

import java.util.concurrent.ThreadLocalRandom;

public enum Category {
    ANIMALS("Животные"),
    MOVIE("Фильмы"),
    GEOGRAPHY("География"),
    MUSIC("Музыка"),
    FOOD("Еда"),
    SPORTS("Спорт"),
    SCIENCE("Наука"),
    HISTORY("История"),
    RANDOM("Случайная категория");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }

    public static Category getRandomCategory() {
        Category[] categories = values();
        if (categories.length == 0) {throw new IllegalStateException("Значения не найдены");}
        return categories[ThreadLocalRandom.current().nextInt(categories.length)];
    }
}
