package academy;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static java.util.Map.entry;

public class AcceptanceTestExample {

    private AcceptanceTestExample() {
    }

    // Используй вызов движка игры вместо хардкода тестовых данных
    public static final Map<String, List<Map.Entry<Predicate<String>, Supplier<String>>>> TEST_CASES_DUMMY = Map.of(
        "волокно", List.of(
            entry("толокно"::equalsIgnoreCase, () -> "*олокно;NEG"),
            entry("барахло"::equalsIgnoreCase, () -> "*оло**о;NEG")
        ),
        "окно", List.of(entry("окно"::equalsIgnoreCase, () -> "окно;POS"))
    );
    public static final List<Map.Entry<Predicate<String>, Supplier<String>>> UNKNOWN_TEST_WORD = List.of(
        entry(_ -> true, () -> "Unknown word")
    );
}
