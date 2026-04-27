package academy.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

    @Test
    @DisplayName("Исключение создаётся с заданным сообщением")
    void test1() {
        String message = "Invalid configuration";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Исключение является наследником RuntimeException")
    void test2() {
        ValidationException exception = new ValidationException("error");

        assertInstanceOf(RuntimeException.class, exception);
    }
}
