package academy.validation;

import static org.junit.jupiter.api.Assertions.*;

import academy.AppConfig;
import academy.affine.AffineTransform;
import academy.exceptions.ValidationException;
import academy.transformations.WeightedTransformation;
import academy.transformations.impl.LinearTransformation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigValidatorTest {

    @Test
    @DisplayName("Корректная конфигурация проходит валидацию")
    void testValidConfig() {
        AppConfig config = createValidConfig();

        assertDoesNotThrow(() -> ConfigValidator.validate(config));
    }

    @Test
    @DisplayName("Некорректная ширина вызывает исключение")
    void testInvalidWidth() {
        AppConfig config = createValidConfig();
        config.width = 0;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Invalid image size"));
    }

    @Test
    @DisplayName("Некорректная высота вызывает исключение")
    void testInvalidHeight() {
        AppConfig config = createValidConfig();
        config.height = -5;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Invalid image size"));
    }

    @Test
    @DisplayName("Нулевое количество итераций вызывает исключение")
    void testZeroIterationCount() {
        AppConfig config = createValidConfig();
        config.iterationCount = 0;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Iteration count must be > 0"));
    }

    @Test
    @DisplayName("Отрицательное количество итераций вызывает исключение")
    void testNegativeIterationCount() {
        AppConfig config = createValidConfig();
        config.iterationCount = -100;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Iteration count must be > 0"));
    }

    @Test
    @DisplayName("Нулевое количество потоков вызывает исключение")
    void testZeroThreads() {
        AppConfig config = createValidConfig();
        config.threads = 0;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Threads must be >= 1"));
    }

    @Test
    @DisplayName("Отрицательное количество потоков вызывает исключение")
    void testNegativeThreads() {
        AppConfig config = createValidConfig();
        config.threads = -2;

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("Threads must be >= 1"));
    }

    @Test
    @DisplayName("Пустой список аффинных преобразований вызывает исключение")
    void testEmptyAffineTransforms() {
        AppConfig config = createValidConfig();
        config.affineTransforms.clear();

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("No affine transforms"));
    }

    @Test
    @DisplayName("Пустой список функций вызывает исключение")
    void testEmptyTransformations() {
        AppConfig config = createValidConfig();
        config.transformations.clear();

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertTrue(exception.getMessage().contains("No functions"));
    }

    @Test
    @DisplayName("Все проверки выполняются")
    void testMultipleValidationErrors() {
        AppConfig config = new AppConfig();
        config.width = 0;
        config.height = 0;
        config.iterationCount = 0;
        config.threads = 0;
        config.affineTransforms = new ArrayList<>();
        config.transformations = new ArrayList<>();

        ValidationException exception = assertThrows(ValidationException.class, () -> ConfigValidator.validate(config));

        assertNotNull(exception.getMessage());
    }

    private AppConfig createValidConfig() {
        AppConfig config = new AppConfig();
        config.width = 800;
        config.height = 600;
        config.iterationCount = 1000000;
        config.threads = 4;

        List<AffineTransform> affines = new ArrayList<>();
        affines.add(new AffineTransform(0.5, 0.0, 0.0, 0.5, 0.0, 0.0));
        config.affineTransforms = affines;

        List<WeightedTransformation> functions = new ArrayList<>();
        functions.add(new WeightedTransformation(new LinearTransformation(), 1.0, 0.5, 0.5, 0.5));
        config.transformations = functions;

        return config;
    }
}
