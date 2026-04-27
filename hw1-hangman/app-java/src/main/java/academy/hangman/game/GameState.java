package academy.hangman.game;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.data.Word;
import java.util.HashSet;
import java.util.Set;

public class GameState {
    private final Category category;
    private final Complexity complexity;
    private final Word secretWord;
    private final Set<Character> guessedLetters;
    private final Set<Character> incorrectGuesses;
    private final int maxAttempts;

    private int attemptsLeft;
    private boolean gameWon;
    private boolean gameOver;

    public GameState(Category category, Complexity complexity, Word secretWord, int maxAttempts) {
        this.category = category;
        this.complexity = complexity;
        this.secretWord = secretWord;
        this.guessedLetters = new HashSet<>();
        this.incorrectGuesses = new HashSet<>();
        this.maxAttempts = maxAttempts;
        this.attemptsLeft = maxAttempts;
        this.gameWon = false;
        this.gameOver = false;
    }

    public Category getCategory() { return category; }
    public Complexity getComplexity() { return complexity; }
    public Word getSecretWord() { return secretWord; }
    public Set<Character> getGuessedLetters() { return guessedLetters; }
    public Set<Character> getIncorrectGuesses() { return incorrectGuesses; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public boolean isGameWon() { return gameWon; }
    public boolean isGameOver() { return gameOver; }

    public boolean makeGuess(char letter) {
        char lowerLetter = Character.toLowerCase(letter);

        if (gameOver) {
            return false; // игра завершена
        }

        if (guessedLetters.contains(lowerLetter) || incorrectGuesses.contains(lowerLetter)) {
            return false; // Буква уже была
        }

        if (secretWord.contains(letter)) {
            guessedLetters.add(lowerLetter);
            checkIfWon();
            return true;
        } else {
            incorrectGuesses.add(lowerLetter);
            attemptsLeft--;
            checkIfLost();
            return false;
        }
    }

    private void checkIfWon() {
        gameWon = secretWord.isGuessed(guessedLetters);
        if (gameWon) {
            gameOver = true;
        }
    }

    private void checkIfLost() {
        if (attemptsLeft <= 0) {
            gameOver = true;
            gameWon = false;
        }
    }

    public String getMaskedWord() {
        return secretWord.getMasked(guessedLetters);
    }

    public int getWrongAttemptsCount() {
        return maxAttempts - attemptsLeft;
    }

    public int getActualMaxAttempts() {
        return maxAttempts;
    }
}
