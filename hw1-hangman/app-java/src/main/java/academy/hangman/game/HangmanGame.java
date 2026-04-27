package academy.hangman.game;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.data.Word;
import academy.hangman.data.WordDictionary;
import academy.hangman.data.WordProvider;
import academy.hangman.ui.HangmanUI;
import java.util.Scanner;

public class HangmanGame implements GameUI{
    private final WordProvider wordProvider;
    private final HangmanUI ui;
    private final Scanner scanner;

    public HangmanGame() {
        this.wordProvider = new WordDictionary();
        this.ui = new HangmanUI();
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void startGame() {
        ui.showWelcomeMessage();

        Category category = ui.getCategoryInput(scanner);
        Complexity complexity = ui.getComplexityInput(scanner);
        int actualAttempts = complexity.getMaxAttempts();
        if (complexity == Complexity.CUSTOM) {
            actualAttempts = ui.getCustomAttempts();
        }
        Word secretWord = wordProvider.getRandomWord(category);

        GameState gameState = new GameState(category, complexity, secretWord, actualAttempts);

        ui.showGameStartInfo(gameState);

        // Игровой цикл
        while (!gameState.isGameOver()) {
            ui.displayGameState(gameState);

            char letter = ui.getPlayerInput(scanner);
            if (letter == '\0') {
                continue;
            }

            boolean correctGuess = gameState.makeGuess(letter);
            ui.showGuessResult(correctGuess, letter, gameState);
        }

        ui.showFinalResult(gameState);
        scanner.close();
    }
}
