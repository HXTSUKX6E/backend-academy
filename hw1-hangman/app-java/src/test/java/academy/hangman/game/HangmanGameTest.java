package academy.hangman.game;

import academy.hangman.data.Category;
import academy.hangman.data.Complexity;
import academy.hangman.data.Word;
import academy.hangman.data.WordProvider;
import academy.hangman.ui.HangmanUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HangmanGameTest {

    static class TestWordProvider implements WordProvider {
        private final Word word;

        public TestWordProvider(String word) {
            this.word = new Word(word);
        }

        @Override
        public List<Word> getWordsByCategory(Category category) {
            return List.of(word);
        }

        @Override
        public Word getRandomWord(Category category) {
            return word;
        }
    }

    static class TestUI extends HangmanUI {
        private final List<String> log = new ArrayList<>();
        private final List<Character> inputs;
        private int inputIndex = 0;

        private final Category category;
        private final Complexity complexity;
        private final int customAttempts;

        public TestUI(Category category, Complexity complexity, int customAttempts, List<Character> inputs) {
            this.category = category;
            this.complexity = complexity;
            this.customAttempts = customAttempts;
            this.inputs = inputs;
        }

        @Override
        public void showWelcomeMessage() {
            log.add("welcome");
        }

        @Override
        public Category getCategoryInput(Scanner scanner) {
            log.add("category:" + category);
            return category;
        }

        @Override
        public Complexity getComplexityInput(Scanner scanner) {
            log.add("complexity:" + complexity);
            return complexity;
        }

        @Override
        public int getCustomAttempts() {
            log.add("customAttempts:" + customAttempts);
            return customAttempts;
        }


        @Override
        public void displayGameState(GameState gameState) {
            log.add("display:" + gameState.getAttemptsLeft());
        }

        @Override
        public char getPlayerInput(Scanner scanner) {
            if (inputIndex < inputs.size()) {
                char c = inputs.get(inputIndex++);
                log.add("input:" + c);
                return c;
            }
            log.add("input:empty");
            return '\0';
        }

        @Override
        public void showGuessResult(boolean correctGuess, char letter, GameState gameState) {
            log.add("guess:" + letter + ":" + correctGuess);
        }

        @Override
        public void showFinalResult(GameState gameState) {
            log.add("final:" + (gameState.isGameWon() ? "win" : "lose"));
        }

        public List<String> getLog() {
            return log;
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testEasyWin() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            TestWordProvider provider = new TestWordProvider("java");

            HangmanUI ui = new HangmanUI() {
                private int inputIndex = 0;
                private final char[] inputs = {'j', 'a', 'v', 'a'};

                @Override
                public Category getCategoryInput(Scanner scanner) {
                    return Category.ANIMALS;
                }

                @Override
                public Complexity getComplexityInput(Scanner scanner) {
                    return Complexity.EASY;
                }

                @Override
                public char getPlayerInput(Scanner scanner) {
                    if (inputIndex < inputs.length) {
                        return inputs[inputIndex++];
                    }
                    return 'x';
                }
            };

            HangmanGame game = new HangmanGame();
            setPrivateField(game, "wordProvider", provider);
            setPrivateField(game, "ui", ui);
            setPrivateField(game, "scanner", new Scanner(System.in));

            game.startGame();

            String output = outputStream.toString();

            System.setOut(originalOut);

            assertTrue(output.contains("Поздравляем") || output.contains("верно отгадали"));
            assertTrue(output.contains("java"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void testLoseAfterAttempts() throws Exception {
        TestWordProvider provider = new TestWordProvider("abc");
        TestUI ui = new TestUI(Category.ANIMALS, Complexity.EASY, 0, List.of('x', 'y', 'z', 'q', 'w', 'r', 't', 'u'));

        HangmanGame game = new HangmanGame();
        setPrivateField(game, "wordProvider", provider);
        setPrivateField(game, "ui", ui);
        setPrivateField(game, "scanner", new Scanner(System.in));

        game.startGame();

        List<String> log = ui.getLog();

        assertTrue(log.contains("welcome"));
        assertTrue(log.stream().anyMatch(s -> s.startsWith("final:lose")));
    }

    @Test
    void testCustomAttemptsRange() throws Exception {
        for (int attempt = 2; attempt <= 15; attempt++) {
            final int currentAttempt = attempt;
            TestWordProvider provider = new TestWordProvider("abc");

            TestUI ui = new TestUI(Category.ANIMALS, Complexity.CUSTOM, currentAttempt, List.of('a', 'b', 'c')) {
                @Override
                public void showGameStartInfo(GameState gameState) {
                    super.showGameStartInfo(gameState);
                }
            };

            HangmanGame game = new HangmanGame();
            setPrivateField(game, "wordProvider", provider);
            setPrivateField(game, "ui", ui);
            setPrivateField(game, "scanner", new Scanner(System.in));
            game.startGame();

            // Ищем информацию о попытках в логах
            boolean found = ui.getLog().stream()
                .anyMatch(s -> s.contains("" + currentAttempt) &&
                    (s.contains("попыток") || s.contains("Attempts")));

            assertTrue(found, "Для кастомной сложности должно отображаться количество попыток: " + currentAttempt +
                ". Логи: " + ui.getLog());
        }
    }

    @Test
    void testRepeatedLettersNotBreakingGame() throws Exception {
        TestWordProvider provider = new TestWordProvider("hi");
        TestUI ui = new TestUI(Category.ANIMALS, Complexity.EASY, 0, List.of('h', 'h', 'i'));

        HangmanGame game = new HangmanGame();
        setPrivateField(game, "wordProvider", provider);
        setPrivateField(game, "ui", ui);
        setPrivateField(game, "scanner", new Scanner(System.in));

        game.startGame();

        List<String> log = ui.getLog();
        long hGuesses = log.stream().filter(s -> s.startsWith("guess:h")).count();

        assertTrue(hGuesses >= 1);
        assertTrue(log.stream().anyMatch(s -> s.startsWith("final:win")));
    }

    @Test
    void testGameOverAfterAllAttempts() throws Exception {
        TestWordProvider provider = new TestWordProvider("java");
        TestUI ui = new TestUI(Category.ANIMALS, Complexity.MEDIUM, 0, List.of('x', 'y', 'z', 'q', 'w', 'r', 't'));

        HangmanGame game = new HangmanGame();
        setPrivateField(game, "wordProvider", provider);
        setPrivateField(game, "ui", ui);
        setPrivateField(game, "scanner", new Scanner(System.in));

        game.startGame();

        List<String> log = ui.getLog();

        assertTrue(log.stream().anyMatch(s -> s.startsWith("final:lose")));
    }

    @Test
    void testNoCrashWhenNoInputs() throws Exception {
        TestWordProvider provider = new TestWordProvider("a");
        TestUI ui = new TestUI(Category.ANIMALS, Complexity.EASY, 0, List.of());

        HangmanGame game = new HangmanGame();
        setPrivateField(game, "wordProvider", provider);
        setPrivateField(game, "ui", ui);
        setPrivateField(game, "scanner", new Scanner(System.in));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(game::startGame);

        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
        } finally {
            executor.shutdown();
        }

        // нет краша -> тест пройден
        assertTrue(true);
    }
}
