package academy.hangman.data;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void testGetDisplayName_ReturnsCorrectValues() {
        assertEquals("Животные", Category.ANIMALS.getDisplayName());
        assertEquals("Фильмы", Category.MOVIE.getDisplayName());
        assertEquals("География", Category.GEOGRAPHY.getDisplayName());
        assertEquals("Музыка", Category.MUSIC.getDisplayName());
        assertEquals("Еда", Category.FOOD.getDisplayName());
        assertEquals("Спорт", Category.SPORTS.getDisplayName());
        assertEquals("Наука", Category.SCIENCE.getDisplayName());
        assertEquals("История", Category.HISTORY.getDisplayName());
        assertEquals("Случайная категория", Category.RANDOM.getDisplayName());
    }

    @RepeatedTest(5)
    void testGetRandomCategory_ReturnsNonNullAndValidValue() {
        Category result = Category.getRandomCategory();

        assertNotNull(result, "Метод не должен возвращать null");

        assertTrue(EnumSet.allOf(Category.class).contains(result),
            "getRandomCategory должен возвращать одно из существующих значений enum");
    }

    @Test
    void testGetRandomCategory_CoversAllCategoriesOverTime() {
        Set<Category> seen = EnumSet.noneOf(Category.class);

        for (int i = 0; i < 100; i++) {
            seen.add(Category.getRandomCategory());
        }

        // хотя бы 5 разных категорий должны попасться за 100 вызовов
        assertTrue(seen.size() >= 5,
            "getRandomCategory должен генерировать разные категории при множественных вызовах");
    }

    @Test
    void testValues_NotEmptyAndContainAll() {
        Category[] values = Category.values();

        assertEquals(9, values.length, "Ожидается 9 категорий");
        for (Category category : values) {
            assertNotNull(category.getDisplayName(), "DisplayName не должен быть null");
        }
    }

    @Test
    void testGetRandomCategory_ThrowsIfEnumEmpty() {
        assertDoesNotThrow(Category::getRandomCategory,
            "Метод не должен выбрасывать исключений при обычной работе");
    }
}
