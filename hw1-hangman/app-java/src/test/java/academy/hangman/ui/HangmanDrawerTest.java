package academy.hangman.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class HangmanDrawerTest {

    private HangmanDrawer drawer;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        drawer = new HangmanDrawer();

        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testDrawHangmanDefault6AttemptsNoWrong() {
        drawer.drawHangman(0); // 0 ошибок, maxAttempts = 6 по умолчанию
        String output = outContent.toString();

        assertTrue(output.contains("│     │")); // виселица рисуется
        assertTrue(output.contains("Ошибок: 0/6"));
    }

    @Test
    void testDrawHangman4AttemptsOneWrong() {
        drawer.drawHangman(1, 4); // 1 ошибка, maxAttempts = 4
        String output = outContent.toString();

        assertTrue(output.contains("O")); // голова должна быть видна
        assertTrue(output.contains("Ошибок: 1/4"));
    }

    @Test
    void testDrawHangman8AttemptsHalfWrong() {
        drawer.drawHangman(4, 8); // 4 ошибки
        String output = outContent.toString();

        assertTrue(output.contains("O")); // голова
        assertTrue(output.contains("/|\\")); // руки
        assertTrue(output.contains("Ошибок: 4/8"));
    }

    @Test
    void testDrawHangmanMaxAttemptsClamped() {
        drawer.drawHangman(3, 15); // maxAttempts > MAX_ATTEMPTS
        String output = outContent.toString();

        assertTrue(output.contains("Ошибок: 3/15"));
    }

    @Test
    void testDrawHangmanMinAttemptsClamped() {
        drawer.drawHangman(1, 1);
        String output = outContent.toString();

        assertTrue(output.contains("Ошибок: 1/1"));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
}
