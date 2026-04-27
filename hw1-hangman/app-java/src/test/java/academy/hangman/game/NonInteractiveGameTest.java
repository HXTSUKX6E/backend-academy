package academy.hangman.game;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonInteractiveGameTest {

    @Test
    void testWinWithExactMatch() {
        NonInteractiveGame game = new NonInteractiveGame("кот", "кот");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("кот;POS"));
    }

    @Test
    void testWinWithDifferentCase() {
        NonInteractiveGame game = new NonInteractiveGame("КОТ", "кот");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("КОТ;POS"));
    }

    @Test
    void testLoseWithWrongLetters() {
        NonInteractiveGame game = new NonInteractiveGame("кот", "дом");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains(";NEG"));
    }

    @Test
    void testDifferentLengthThrowsException() {
        NonInteractiveGame game = new NonInteractiveGame("привет", "бай");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            game::startGame
        );
        assertTrue(exception.getMessage().contains("одинаковой длины"));
    }

    @Test
    void testRussianLettersWin() {
        NonInteractiveGame game = new NonInteractiveGame("программирование", "программирование");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("программирование;POS"));
    }


    @Test
    void testMixedRussianEnglish() {
        NonInteractiveGame game = new NonInteractiveGame("java", "java");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("java;POS"));
    }

    @Test
    void testSingleRussianLetterWin() {
        NonInteractiveGame game = new NonInteractiveGame("я", "я");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("я;POS"));
    }

    @Test
    void testSingleRussianLetterLose() {
        NonInteractiveGame game = new NonInteractiveGame("я", "ю");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        String result = output.toString().trim();

        assertTrue(result.contains(";NEG"), "Должен быть ;NEG в выводе: " + result);
    }

    @Test
    void testDuplicateRussianLetters() {
        NonInteractiveGame game = new NonInteractiveGame("мама", "мама");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        game.startGame();

        assertTrue(output.toString().contains("мама;POS"));
    }
}
