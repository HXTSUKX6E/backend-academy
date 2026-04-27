package academy.hangman.data;

import java.util.concurrent.ThreadLocalRandom;

public enum Complexity {
    EASY("Легкий", 8),
    MEDIUM("Средний", 6),
    HARD("Сложный", 4),
    RANDOM("Случайная сложность", -1),
    CUSTOM("Пользовательский", -1);

    private final String displayName;
    private final int maxAttempts;

    Complexity(String displayName, int maxAttempts) {
        this.displayName = displayName;
        this.maxAttempts = maxAttempts;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public static Complexity getRealRandomComplexity() {
        Complexity[] realComplexities = {EASY, MEDIUM, HARD};
        return realComplexities[ThreadLocalRandom.current().nextInt(realComplexities.length)];
    }
}
