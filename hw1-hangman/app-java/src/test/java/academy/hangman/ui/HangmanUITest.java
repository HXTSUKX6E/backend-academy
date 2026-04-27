package academy.hangman.ui;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.data.Word;
import academy.hangman.game.GameState;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class HangmanUITest {

    private HangmanUI ui;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        ui = new HangmanUI();

        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testShowWelcomeMessage() {
        ui.showWelcomeMessage();
        String output = outContent.toString();
        assertTrue(output.contains("=== ИГРА 'ВИСЕЛИЦА' ==="));
        assertTrue(output.contains("Добро пожаловать в игру!"));
    }

    @Test
    void testGetPlayerInputValidLetter() {
        Scanner scanner = new Scanner("a\n");
        char letter = ui.getPlayerInput(scanner);
        assertEquals('a', letter);
    }

    @Test
    void testGetPlayerInputEmpty() {
        Scanner scanner = new Scanner("\n");
        char letter = ui.getPlayerInput(scanner);
        assertEquals('\0', letter);
        assertTrue(outContent.toString().contains("Пожалуйста, введите букву!"));
    }

    @Test
    void testGetPlayerInputMultipleChars() {
        Scanner scanner = new Scanner("ab\n");
        char letter = ui.getPlayerInput(scanner);
        assertEquals('\0', letter);
        assertTrue(outContent.toString().contains("Пожалуйста, введите только одну букву!"));
    }

    @Test
    void testGetPlayerInputNonLetter() {
        Scanner scanner = new Scanner("1\n");
        char letter = ui.getPlayerInput(scanner);
        assertEquals('\0', letter);
        assertTrue(outContent.toString().contains("Пожалуйста, введите букву"));
    }

    @Test
    void testGetCustomAttemptsInputValid() {
        Scanner scanner = new Scanner("10\n");
        int attempts = ui.getCustomAttemptsInput(scanner);
        assertEquals(10, attempts);
    }

    @Test
    void testGetCustomAttemptsInputInvalidThenValid() {
        Scanner scanner = new Scanner("1\n20\n5\n");
        int attempts = ui.getCustomAttemptsInput(scanner);
        assertEquals(5, attempts);
        String output = outContent.toString();
        assertTrue(output.contains("Ошибка, введите число от 2 до 15"));
    }

    @Test
    void testShowGameStartInfo() {
        GameState gameState = new GameState(Category.ANIMALS, Complexity.EASY, new Word("ТЕСТ"), 6);
        ui.showGameStartInfo(gameState);
        String output = outContent.toString();
        assertTrue(output.contains("Выбранные настройки игры"));
        assertTrue(output.contains("ИГРА НАЧАЛАСЬ"));
        assertTrue(output.contains("Категория:"));
        assertTrue(output.contains("Сложность:"));
    }

    @Test
    void testShowFinalResultWin() {
        GameState gameState = new GameState(Category.ANIMALS, Complexity.EASY, new Word("ТЕСТ"), 6);
        gameState.makeGuess('Т');
        gameState.makeGuess('Е');
        gameState.makeGuess('С');
        gameState.makeGuess('Т');
        ui.showFinalResult(gameState);
        String output = outContent.toString();
        assertTrue(output.contains("Поздравляем"));
        assertTrue(output.contains("Загаданное слово: ТЕСТ"));
    }

    @Test
    void testShowFinalResultLose() {
        GameState gameState = new GameState(Category.ANIMALS, Complexity.EASY, new Word("ТЕСТ"), 2);
        gameState.makeGuess('А');
        gameState.makeGuess('Б');
        ui.showFinalResult(gameState);
        String output = outContent.toString();
        assertTrue(output.contains("вы проиграли"));
        assertTrue(output.contains("Загаданное слово: ТЕСТ"));
    }
}
