package academy.hangman.data;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WordTest {

    @Test
    void testConstructor_ValidWord() {
        Word word = new Word("Корова");
        assertEquals("Корова", word.getOriginal());
        assertEquals(6, word.getLength());
    }

    @Test
    void testConstructor_TrimsWhitespace() {
        Word word = new Word("  Панда  ");
        assertEquals("Панда", word.getOriginal(), "Лишние пробелы удаляются");
    }

    @Test
    void testConstructor_ThrowsExceptionOnEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Word(""),
            "Пустая строка вызывает исключение");
    }

    @Test
    void testConstructor_ThrowsExceptionOnNull() {
        assertThrows(IllegalArgumentException.class, () -> new Word(null),
            "null должен вызывать исключение");
    }

    @Test
    void testContains_LetterExists() {
        Word word = new Word("Корова");
        assertTrue(word.contains('о'), "Буква 'о' есть в слове");
    }

    @Test
    void testContains_LetterExistsIgnoringCase() {
        Word word = new Word("Панда");
        assertTrue(word.contains('п'), "Метод должен игнорировать регистр");
        assertTrue(word.contains('П'), "Метод должен игнорировать регистр");
    }

    @Test
    void testContains_LetterNotExists() {
        Word word = new Word("Панда");
        assertFalse(word.contains('ж'), "Буквы 'ж' нет в слове");
    }

    @Test
    void testGetMasked_NoGuessedLetters() {
        Word word = new Word("Панда");
        String masked = word.getMasked(Set.of());
        assertEquals("*****", masked, "Все буквы должны быть скрыты");
    }

    @Test
    void testGetMasked_SomeGuessedLetters() {
        Word word = new Word("Панда");
        String masked = word.getMasked(Set.of('п', 'а'));
        assertEquals("Па**а", masked, "Должны отображаться только угаданные буквы");
    }


    @Test
    void testIsGuessed_AllLettersGuessed() {
        Word word = new Word("Кот");
        assertTrue(word.isGuessed(Set.of('к', 'о', 'т')),
            "Все буквы угаданы");
    }

    @Test
    void testIsGuessed_NotAllLettersGuessed() {
        Word word = new Word("Кот");
        assertFalse(word.isGuessed(Set.of('к', 'т')),
            "Не все буквы угаданы");
    }

    @Test
    void testEquals_IgnoresCase() {
        Word w1 = new Word("Панда");
        Word w2 = new Word("пАНДА");
        assertEquals(w1, w2, "Слова с разным регистром должны считаться равными");
    }

    @Test
    void testEquals_DifferentWords() {
        Word w1 = new Word("Панда");
        Word w2 = new Word("Медведь");
        assertNotEquals(w1, w2);
    }

    @Test
    void testHashCode_ConsistentWithEquals() {
        Word w1 = new Word("Крокодил");
        Word w2 = new Word("крокодил");
        assertEquals(w1.hashCode(), w2.hashCode(),
            "hashCode должны быть равны");
    }

    @Test
    void testToString_ReturnsOriginalWord() {
        Word word = new Word("Ёж");
        assertEquals("Ёж", word.toString());
    }
}
