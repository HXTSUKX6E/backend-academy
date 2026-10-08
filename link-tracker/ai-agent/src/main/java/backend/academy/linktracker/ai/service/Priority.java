package backend.academy.linktracker.ai.service;

public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    public static Priority max(Priority a, Priority b) {
        return a.ordinal() <= b.ordinal() ? a : b;
    }
}
