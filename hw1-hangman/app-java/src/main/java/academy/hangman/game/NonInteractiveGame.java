package academy.hangman.game;

import academy.AppConfig;
import academy.hangman.data.Category;
import academy.hangman.data.Word;
import java.util.HashSet;
import java.util.Set;

public class NonInteractiveGame implements GameUI{

    private final String secretWord;
    private final String userInput;

    public NonInteractiveGame(String secretWord, String userInput) {
        this.secretWord = secretWord;
        this.userInput = userInput;
    }

    public void startGame() {

        if (secretWord.length() != userInput.length()) {
            throw new IllegalArgumentException(
                "Слова должны быть одинаковой длины. secretWord: " + secretWord.length() +
                    ", userInput: " + userInput.length()
            );
        }

        Word word = new Word(secretWord);
        int maxAttempts = secretWord.length() * 2;
        GameState gameState = new GameState(Category.RANDOM, null, word, maxAttempts);
        processUserInput(gameState, userInput);
        System.out.println(formatResult(gameState, secretWord));
    }

    private void processUserInput(GameState gameState, String userInput) {

        Set<Character> guessedLetters = new HashSet<>();
        for (char c : userInput.toCharArray()) {
            guessedLetters.add(Character.toLowerCase(c));
        }

        for (char letter : guessedLetters) {
            if (gameState.isGameOver()) break;
            gameState.makeGuess(letter);
        }
    }

    private String formatResult(GameState gameState, String originalWord) {
        String maskedWord = gameState.getMaskedWord();

        if (gameState.isGameWon()) {
            return originalWord + ";POS";
        } else {
            return maskedWord + ";NEG";
        }
    }
}
