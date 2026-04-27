package academy;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class ApplicationTest {

    @Test
    void main_WithHelpOption_ReturnsSuccess() {
        String[] args = {"--help"};
        int exitCode = new CommandLine(new Application()).execute(args);
        assertEquals(0, exitCode);
    }

    @Test
    void generateCommand_WithValidParameters_ReturnsSuccess(@TempDir Path tempDir) {
        String[] args = {
            "generate",
            "--algorithm",
            "dfs",
            "--width",
            "5",
            "--height",
            "5",
            "--output",
            tempDir.resolve("maze.txt").toString()
        };

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("maze.txt")));
    }

    @Test
    void generateCommand_WithInvalidAlgorithm_ReturnsError() {
        String[] args = {"generate", "--algorithm", "invalid", "--width", "5", "--height", "5"};

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(1, exitCode);
    }

    @Test
    void generateCommand_WithInvalidDimensions_ReturnsError() {
        String[] args = {"generate", "--algorithm", "dfs", "--width", "-1", "--height", "5"};

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(1, exitCode);
    }

    @Test
    void solveCommand_WithValidParameters_ReturnsSuccess(@TempDir Path tempDir) throws IOException {
        File mazeFile = tempDir.resolve("test_maze.txt").toFile();
        Files.writeString(mazeFile.toPath(), "###\n" + "# #\n" + "###");

        String[] args = {"solve", "--algorithm", "bfs", "--file", mazeFile.toString(), "--start", "1,1", "--end", "1,1"
        };

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(0, exitCode);
    }

    @Test
    void solveCommand_WithInvalidFile_ReturnsError(@TempDir Path tempDir) {
        String[] args = {
            "solve",
            "--algorithm",
            "bfs",
            "--file",
            tempDir.resolve("nonexistent.txt").toString(),
            "--start",
            "1,1",
            "--end",
            "3,3"
        };

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(1, exitCode);
    }

    @Test
    void solveCommand_WithInvalidPointFormat_ReturnsError(@TempDir Path tempDir) throws IOException {
        File mazeFile = tempDir.resolve("test_maze.txt").toFile();
        Files.writeString(mazeFile.toPath(), "  \n # \n");

        String[] args = {
            "solve", "--algorithm", "bfs", "--file", mazeFile.toString(), "--start", "invalid", "--end", "1,1"
        };

        int exitCode = new CommandLine(new Application()).execute(args);

        assertEquals(1, exitCode);
    }
}
