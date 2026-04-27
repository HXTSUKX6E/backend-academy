package academy;

import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public record AppConfig(int fontSize, String[] words) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AppConfig appConfig = (AppConfig) o;
        return fontSize == appConfig.fontSize && Objects.deepEquals(words, appConfig.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontSize, Arrays.hashCode(words));
    }

    @Override
    @NotNull
    public String toString() {
        return "AppConfig{" + "fontSize=" + fontSize +
            ", words=" + (words == null ? "null" : Arrays.asList(words).toString()) +
            '}';
    }
}
