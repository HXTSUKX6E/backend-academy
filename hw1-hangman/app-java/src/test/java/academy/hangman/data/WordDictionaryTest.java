package academy.hangman.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordDictionaryTest {

    private WordDictionary dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new WordDictionary();
    }

    @Test
    void testGetRandomWord_NotNull() {
        Word word = dictionary.getRandomWord(Category.ANIMALS);
        assertNotNull(word, "Случайное слово не должно быть null");
    }

    @Test
    void testGetRandomWord_BelongsToCategory() {
        List<Word> animalWords = dictionary.getWordsByCategory(Category.ANIMALS);
        Word randomWord = dictionary.getRandomWord(Category.ANIMALS);

        assertTrue(animalWords.contains(randomWord),
            "Случайное слово должно принадлежать заданной категории");
    }

    @Test
    void testGetRandomWord_ReturnsDifferentValues() {
        Set<Word> rnd = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            rnd.add(dictionary.getRandomWord(Category.MOVIE));
        }

        assertTrue(rnd.size() > 1,
            "Метод должен иногда возвращать разные слова при повторных вызовах");
    }

    @Test
    void testGetWordsByCategory_ReturnsUnmodifiableList() {
        List<Word> foodWords = dictionary.getWordsByCategory(Category.FOOD);
        assertThrows(UnsupportedOperationException.class, () -> foodWords.add(new Word("Сосиска")),
            "Список слов должен быть неизменяемым");
    }

    @Test
    void testGetWordsByCategory_Random_ReturnsAllWords() {
        int total = dictionary.getTotalWordsCount();
        List<Word> allWords = dictionary.getWordsByCategory(Category.RANDOM);

        assertEquals(total, allWords.size(),
            "Для категории RANDOM должны возвращаться все слова из всех категорий");
    }

    @Test
    void testTotalWordsCount_IsCorrect() {
        int count = dictionary.getTotalWordsCount();
        assertTrue(count > 0, "Количество слов должно быть положительным");
        assertEquals(15 * 8, count, "Общее количество слов должно равняться 120 (8 категорий × 15 слов)");
    }
}
