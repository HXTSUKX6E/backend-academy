package academy.hangman.ui;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.game.GameState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class HangmanUI {
    private final HangmanDrawer drawer;

    private int customAttempts;

    public int getCustomAttempts() {
        return customAttempts;
    }

    public HangmanUI() {
        this.drawer = new HangmanDrawer();
    }

    private static final String WELCOME_MESSAGE = """
        === ИГРА 'ВИСЕЛИЦА' ===
        Добро пожаловать в игру!
        """;

    private static final String GAME_START_MESSAGE = """

        ══════════════════════════════
           🚀   ИГРА НАЧАЛАСЬ!   🚀
        ══════════════════════════════

       """;

    private static final String CATEGORY_SELECTION = """
        ╔════════════════════════════════════╗
        ║         ВЫБЕРИТЕ КАТЕГОРИЮ         ║
        ╚════════════════════════════════════╝
        """;

    private static final String DIFFICULTY_SELECTION = """
        ╔════════════════════════════════════╗
        ║         ВЫБЕРИТЕ СЛОЖНОСТЬ         ║
        ╚════════════════════════════════════╝
        """;

    private static final String DIFFICULTY_OPTIONS = """
        1. 🟢 Легкий (8 попыток)
        2. 🟡 Средний (6 попыток)
        3. 🔴 Сложный (4 попыток)
        4. 🎲 Случайная сложность
        5. ⚙️ Пользовательский
        """;

    private static final String WORD_DISPLAY= """
        ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄
           СЛОВО: %s
        ▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀
        """;

    private static final String GAME_SETTINGS = """

        ⚙️ Выбранные настройки игры ⚙️
        """;

    private static final String CORRECT_GUESS= """
        ----------------------------------------------
        ✅ Правильно! Буква '%s' есть в слове.
        ----------------------------------------------
        """;

    private static final String INCORRECT_GUESS= """
        ----------------------------------------------
        ❌ Неправильно! Буквы '%s' нет в слове.
        ----------------------------------------------
        """;

    private static final String WIN_MESSAGE = """
        ----------------------------------------------
        🎉 Поздравляем! Вы верно отгадали слово!!!
        Загаданное слово: %s
        ----------------------------------------------
        """;

    private static final String LOSE_MESSAGE = """
        ----------------------------------------------
        💀 К сожалению, вы проиграли...
        Загаданное слово: %s
        ----------------------------------------------
        """;

    public void showWelcomeMessage() {
        System.out.println(WELCOME_MESSAGE);
    }

    public void showGameStartMessage() {
        System.out.print(GAME_START_MESSAGE);
    }

    public void showCategorySelection() {
        System.out.print(CATEGORY_SELECTION);
    }

    public void showWinMessage(String secretWord) {
        System.out.printf(WIN_MESSAGE, secretWord);
    }

    public void showLoseMessage(String secretWord) {
        System.out.printf(LOSE_MESSAGE, secretWord);
    }

    public void showDifficultySelectionMenu() {
        System.out.print(DIFFICULTY_SELECTION);
        System.out.print(DIFFICULTY_OPTIONS);
    }

    public void showCurrentWord(String maskedWord) {
        System.out.printf(WORD_DISPLAY, maskedWord);
    }

    public void showGameSettingsHeader() {
        System.out.print(GAME_SETTINGS);
    }

    public void showCorrectGuess(char letter) {
        System.out.printf(CORRECT_GUESS, letter);
    }

    public void showIncorrectGuess(char letter) {
        System.out.printf(INCORRECT_GUESS, letter);
    }

    private void gameInfo(GameState gameState) {
        System.out.println("Категория: " + gameState.getCategory().getDisplayName());
        System.out.println("Сложность: " + gameState.getComplexity().getDisplayName());
        System.out.println("Длина слова: " + gameState.getSecretWord().getLength());
        System.out.println("Осталось попыток: " + gameState.getAttemptsLeft());
    }

    public void showGameStartInfo(GameState gameState) {
        showGameSettingsHeader();
        gameInfo(gameState);
        System.out.println();

        showGameStartMessage();
    }

    public void displayGameState(GameState gameState) {
        showCurrentWord(gameState.getMaskedWord());

        Set<Character> incorrectGuesses = gameState.getIncorrectGuesses();
        if (!incorrectGuesses.isEmpty()) {
            List<Character> sortedIncorrect = new ArrayList<>(incorrectGuesses);
            Collections.sort(sortedIncorrect);
            System.out.println("Неправильные буквы: " + sortedIncorrect.toString()
                .replace("[", "")
                .replace("]", ""));
        }

        gameInfo(gameState);
        System.out.println();

        drawer.drawHangman(gameState.getWrongAttemptsCount(), gameState.getActualMaxAttempts());
        System.out.println();
    }

    public char getPlayerInput(Scanner scanner) {
        System.out.print("Введите букву: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Пожалуйста, введите букву!");
            System.out.println();
            return '\0';
        }

        if (input.length() > 1) {
            System.out.println("Пожалуйста, введите только одну букву!");
            System.out.println();
            return '\0';
        }

        char letter = input.charAt(0);
        if (!Character.isLetter(letter)) {
            System.out.println("Пожалуйста, введите букву (не цифру и не символ)!");
            System.out.println();
            return '\0';
        }

        return Character.toLowerCase(letter);
    }

    public void showGuessResult(boolean correct, char letter, GameState gameState) {
        if (correct) showCorrectGuess(letter);
        else showIncorrectGuess(letter);
        System.out.println();
    }

    public void showFinalResult(GameState gameState) {
        System.out.println("\n=== ИГРА ОКОНЧЕНА ===");

        if (gameState.isGameWon()) {
            showWinMessage(gameState.getSecretWord().getOriginal());
        } else {
            showLoseMessage(gameState.getSecretWord().getOriginal());
            drawer.drawHangman(gameState.getWrongAttemptsCount(), gameState.getActualMaxAttempts());
        }
    }

    public Category getCategoryInput(Scanner scanner) {
        showCategorySelection();

        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%d. %s\n", i + 1, categories[i].getDisplayName());
        }

        while (true) {
            System.out.print("\nВведите номер категории (1-" + categories.length + "): ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= categories.length) {
                    Category selected = categories[choice - 1];

                    if (selected == Category.RANDOM) {
                        Category randomCategory = Category.getRandomCategory();
                        System.out.println("🎲 Случайная категория: " + randomCategory.getDisplayName());
                        System.out.println();
                        return randomCategory;
                    }

                    System.out.println("✅ Выбрана категория: " + selected.getDisplayName());
                    System.out.println();
                    return selected;

                } else {
                    System.out.println("Ошибка, введите число от 1 до " + categories.length);
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка, введите корректный номер!");
            }
        }
    }

    public Complexity getComplexityInput(Scanner scanner) {
        showDifficultySelectionMenu();

        while (true) {
            System.out.print("\nВыберите сложность (1-5): ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 5) {
                    switch (choice) {
                        case 1:
                            System.out.println("✅ Выбрана сложность: Легкий (8 попыток)");
                            return Complexity.EASY;
                        case 2:
                            System.out.println("✅ Выбрана сложность: Средний (6 попыток)");
                            return Complexity.MEDIUM;
                        case 3:
                            System.out.println("✅ Выбрана сложность: Сложный (4 попытки)");
                            return Complexity.HARD;
                        case 4:
                            Complexity randomComplexity = Complexity.getRealRandomComplexity();
                            System.out.println("🎲 Случайная сложность: " +
                                randomComplexity.getDisplayName());
                            return randomComplexity;
                        case 5:
                            customAttempts = getCustomAttemptsInput(scanner);
                            return Complexity.CUSTOM;
                    }
                } else {
                    System.out.println("❌ Ошибка, введите число от 1 до 5");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка, введите корректный номер!");
            }
        }
    }

    public int getCustomAttemptsInput(Scanner scanner) {
        while (true) {
            System.out.print("🔢 Введите количество попыток (2-15): ");
            String input = scanner.nextLine().trim();

            try {
                int attempts = Integer.parseInt(input);
                if (attempts >= 2 && attempts <= 15) {
                    System.out.println("✅ Количество установленных попыток: " + attempts);
                    System.out.println();
                    return attempts;
                } else {
                    System.out.println("❌ Ошибка, введите число от 2 до 15");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка, введите корректное число!");
            }
        }
    }
}
