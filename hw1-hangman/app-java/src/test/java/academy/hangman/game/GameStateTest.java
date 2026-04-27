package academy.hangman.game;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.data.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState gameState;
    private Word secretWord;

    @BeforeEach
    void setUp() {
        secretWord = new Word("ТЕСТ");
        gameState = new GameState(Category.ANIMALS, Complexity.EASY, secretWord, 3);
    }

    @Test
    void testInitialState() {
        assertEquals(Category.ANIMALS, gameState.getCategory());
        assertEquals(Complexity.EASY, gameState.getComplexity());
        assertEquals(secretWord, gameState.getSecretWord());
        assertTrue(gameState.getGuessedLetters().isEmpty());
        assertTrue(gameState.getIncorrectGuesses().isEmpty());
        assertEquals(3, gameState.getAttemptsLeft());
        assertFalse(gameState.isGameWon());
        assertFalse(gameState.isGameOver());
    }

    // Угадывания
    @Test
    void testCorrectGuess() {
        boolean result = gameState.makeGuess('Т');
        assertTrue(result);
        assertTrue(gameState.getGuessedLetters().contains('т'));
        assertEquals(3, gameState.getAttemptsLeft());
        assertFalse(gameState.isGameOver());
    }

    @Test
    void testIncorrectGuess() {
        boolean result = gameState.makeGuess('А');
        assertFalse(result);
        assertTrue(gameState.getIncorrectGuesses().contains('а'));
        assertEquals(2, gameState.getAttemptsLeft());
        assertFalse(gameState.isGameOver());
    }

    @Test
    void testRepeatedCorrectGuessDoesNotDecreaseAttempts() {
        gameState.makeGuess('Т');
        int attemptsBefore = gameState.getAttemptsLeft();
        boolean result = gameState.makeGuess('т');
        int attemptsAfter = gameState.getAttemptsLeft();

        assertFalse(result);
        assertEquals(attemptsBefore, attemptsAfter);
    }

    @Test
    void testRepeatedIncorrectGuessDoesNotDecreaseAttempts() {
        gameState.makeGuess('А');
        int attemptsBefore = gameState.getAttemptsLeft();
        boolean result = gameState.makeGuess('а');
        int attemptsAfter = gameState.getAttemptsLeft();

        assertFalse(result);
        assertEquals(attemptsBefore, attemptsAfter);
    }

    @Test
    void testCaseInsensitiveGuess() {
        boolean lowerResult = gameState.makeGuess('т');
        boolean upperResult = gameState.makeGuess('Е');

        assertTrue(lowerResult);
        assertTrue(upperResult);
        assertTrue(gameState.getGuessedLetters().contains('т'));
        assertTrue(gameState.getGuessedLetters().contains('е'));
    }

    @Test
    void testWinCondition() {
        gameState.makeGuess('Т');
        gameState.makeGuess('Е');
        gameState.makeGuess('С');
        assertTrue(gameState.isGameWon());
        assertTrue(gameState.isGameOver());
    }

    @Test
    void testWinOnLastAttempt() {
        gameState = new GameState(Category.ANIMALS, Complexity.EASY, secretWord, 3);
        gameState.makeGuess('А');
        gameState.makeGuess('Е');
        gameState.makeGuess('Т');
        gameState.makeGuess('С');

        assertTrue(gameState.isGameWon());
        assertTrue(gameState.isGameOver());
        assertEquals(2, gameState.getAttemptsLeft());
    }

    @Test
    void testLoseCondition() {
        gameState.makeGuess('А');
        gameState.makeGuess('Б');
        gameState.makeGuess('В');
        assertFalse(gameState.isGameWon());
        assertTrue(gameState.isGameOver());
        assertEquals(0, gameState.getAttemptsLeft());
    }

    @Test
    void testGuessAfterGameOverDoesNothing() {
        gameState.makeGuess('А');
        gameState.makeGuess('Б');
        gameState.makeGuess('В');
        assertTrue(gameState.isGameOver());

        boolean result = gameState.makeGuess('Т');
        assertFalse(result);
        assertFalse(gameState.isGameWon());
        assertEquals(0, gameState.getAttemptsLeft());
    }

    @Test
    void testWrongAttemptsCount() {
        gameState.makeGuess('А');
        gameState.makeGuess('Б');
        assertEquals(2, gameState.getWrongAttemptsCount());
    }

    @Test
    void testActualMaxAttempts() {
        assertEquals(3, gameState.getActualMaxAttempts());
    }
}
