package academy.maze.ui;

import academy.maze.Generator;
import academy.maze.Solver;
import academy.maze.display.MazePrinter;
import academy.maze.dto.Maze;
import academy.maze.dto.Path;
import academy.maze.dto.Point;
import academy.maze.factory.AlgorithmFactory;
import academy.maze.utils.MazeUtils;
import academy.maze.utils.PointUtils;
import java.io.File;
import java.util.Scanner;

public class InteractiveMode {
    private final MazePrinter mazePrinter;
    private final Scanner scanner;

    private Maze currentMaze;
    private boolean useUnicode;

    public InteractiveMode(MazePrinter mazePrinter) {
        this.mazePrinter = mazePrinter;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        showMainMenu();
    }

    private void showMainMenu() {
        while (true) {
            String mainMenu =
                    """

                ======ГЛАВНОЕ МЕНЮ======
                1. Сгенерировать лабиринт
                2. Решить лабиринт
                3. Выход
                """;
            System.out.println(mainMenu);
            System.out.print("➤ Введите выбор (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    generateMazeFlow();
                    break;
                case "2":
                    solveMazeFlow();
                    break;
                case "3":
                    System.out.println("УСЁ!");
                    return;
                default:
                    System.out.println("Неверный выбор! Пожалуйста, введите 1, 2 или 3.");
            }
        }
    }

    private void generateMazeFlow() {
        try {
            showWelcome();

            String currentAlgorithm = showChooseAlgorithm();

            int width = getValidatedSize("ширину");
            int height = getValidatedSize("высоту");

            useUnicode = chooseDisplayType();

            generateMaze(currentAlgorithm, width, height, useUnicode);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void solveMazeFlow() {
        if (currentMaze == null) {
            System.out.println("Текущий лабиринт отсутствует");
            solveMazeFromFile();
            return;
        }

        String choiceMenu =
                """

        РЕШЕНИЕ ЛАБИРИНТА
        1. Решить текущий лабиринт
        2. Загрузить из файла
        """;
        System.out.println(choiceMenu);
        System.out.print("➤ Введите выбор (1-2): ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                solveCurrentMaze();
                break;
            case "2":
                solveMazeFromFile();
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void solveCurrentMaze() {
        try {
            System.out.println("\nРЕШЕНИЕ ТЕКУЩЕГО ЛАБИРИНТА");
            int width = currentMaze.cells()[0].length - 2;
            int height = currentMaze.cells()[1].length - 2;
            System.out.println("Размер: " + width + "x" + height);

            showLegend(useUnicode);
            System.out.println();
            mazePrinter.printMazeToConsole(currentMaze, useUnicode);

            System.out.println("\nВведите координаты точек:");
            Point startPoint = getPointInput("стартовой точки");
            Point endPoint = getPointInput("конечной точки");

            try {
                MazeUtils.validatePoint(currentMaze, startPoint, "Стартовая");
                MazeUtils.validatePoint(currentMaze, endPoint, "Конечная");
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
                return;
            }

            String solverAlgorithm = chooseSolverAlgorithm();

            solveAndShowMaze(solverAlgorithm, startPoint, endPoint, useUnicode);

        } catch (Exception e) {
            System.err.println("❌ Ошибка при решении лабиринта: " + e.getMessage());
        }
    }

    private void solveMazeFromFile() {
        try {
            System.out.println("\n📂 РЕШЕНИЕ ЛАБИРИНТА ИЗ ФАЙЛА");

            System.out.print("Введите путь к файлу лабиринта: ");
            String filePath = scanner.nextLine().trim();

            if (filePath.isEmpty()) {
                System.out.println("Путь к файлу не может быть пустым!");
                return;
            }

            @SuppressWarnings("NullAway")
            File mazeFile = java.nio.file.Path.of(filePath).toFile();

            if (!mazeFile.exists()) {
                System.out.println("Файл не найден: " + filePath);
                return;
            }

            currentMaze = MazeUtils.loadMazeFromFile(mazeFile);
            System.out.println("✅ Лабиринт загружен!");
            int width = currentMaze.cells()[0].length - 2;
            int height = currentMaze.cells()[1].length - 2;
            System.out.println("Размер: " + width + "x" + height);

            boolean fileUseUnicode = chooseDisplayType();

            showLegend(fileUseUnicode);
            System.out.println();
            mazePrinter.printMazeToConsole(currentMaze, fileUseUnicode);

            System.out.println("\nВведите координаты точек:");
            Point startPoint = getPointInput("стартовой точки");
            Point endPoint = getPointInput("конечной точки");

            try {
                MazeUtils.validatePoint(currentMaze, startPoint, "Стартовая");
                MazeUtils.validatePoint(currentMaze, endPoint, "Конечная");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }

            String solverAlgorithm = chooseSolverAlgorithm();

            solveAndShowMaze(solverAlgorithm, startPoint, endPoint, fileUseUnicode);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    private Point getPointInput(String pointName) {
        while (true) {
            System.out.print("➤ Введите " + pointName + " (x,y): ");
            String input = scanner.nextLine().trim();
            try {
                return PointUtils.parsePoint(input);
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage() + " Используйте формат x,y (например, 1,1)");
            }
        }
    }

    private String chooseSolverAlgorithm() {
        while (true) {
            String solvers =
                    """

                Выберите алгоритм решения:
                1. A*star
                2. Дейкстра
                3. BFS
                """;
            System.out.println(solvers);
            System.out.print("➤ Введите выбор (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    return "astar";
                case "2":
                    return "dijkstra";
                case "3":
                    return "bfs";
                default:
                    System.out.println("Неверный выбор! Пожалуйста, введите 1, 2 или 3.");
            }
        }
    }

    private void solveAndShowMaze(String solverAlgorithm, Point start, Point end, boolean useUnicode) {
        try {
            System.out.println("\nАлгоритм: " + getSolverAlgorithmName(solverAlgorithm) + "...");
            System.out.println("Стартовая точка: " + start + ", Финишная точка: " + end);

            Solver solver = AlgorithmFactory.createSolver(solverAlgorithm);
            Path path = solver.solve(currentMaze, start, end);

            if (path.points().length == 0) {
                System.out.println("Путь не найден!");
                return;
            }

            System.out.println("✅ Путь найден! Длина пути: " + path.points().length + " шагов");

            showLegend(useUnicode);
            System.out.println();
            mazePrinter.printSolutionToConsole(currentMaze, path, useUnicode);

            saveSolutionToFile(path);

        } catch (Exception e) {
            System.err.println("Ошибка при решении: " + e.getMessage());
        }
    }

    private void saveSolutionToFile(Path path) {
        String saveMenu = """

        💾 Сохранить решение в файл?
        1. Да
        2. Нет
        """;
        System.out.println(saveMenu);
        System.out.print("➤ Введите выбор (1-2): ");

        String saveChoice = scanner.nextLine().trim();

        if (saveChoice.equals("1")) {
            while (true) {
                System.out.print("📁 Введите имя файла (например, solution.txt): ");
                String fileName = scanner.nextLine().trim();

                if (fileName.isEmpty()) {
                    System.out.println("Имя файла не может быть пустым! Пожалуйста, введите имя файла.");
                    continue;
                }

                if (!fileName.endsWith(".txt")) {
                    fileName += ".txt";
                }

                try {
                    File outputFile = java.nio.file.Path.of(fileName).toFile();
                    mazePrinter.saveSolutionToFile(currentMaze, path, outputFile);
                    System.out.println("✅ Решение сохранено в файл: " + outputFile.getPath());
                    break;
                } catch (Exception e) {
                    System.err.println("Ошибка при сохранении файла: " + e.getMessage());
                    System.out.println("Пожалуйста, попробуйте другое имя файла.");
                }
            }
        } else if (!saveChoice.equals("2")) {
            System.out.println("Неверный выбор, файл не сохранен.");
        }
    }

    private String getAlgorithmName(String algorithm) {
        return switch (algorithm) {
            case "dfs" -> "DFS (поиск в глубину)";
            case "prim" -> "Алгоритм Прима";
            case "kruskal" -> "Алгоритм Краскала";
            default -> algorithm;
        };
    }

    private String getSolverAlgorithmName(String algorithm) {
        return switch (algorithm) {
            case "astar" -> "A*";
            case "dijkstra" -> "Дейкстра";
            case "bfs" -> "BFS";
            default -> algorithm;
        };
    }

    private int getValidatedSize(String dimension) {
        while (true) {
            System.out.print("Введите значение, " + dimension.toLowerCase() + " (1-50): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Ошибка. Неправильна задана " + dimension);
                continue;
            }

            try {
                int size = Integer.parseInt(input);
                if (size >= 1 && size <= 50) {
                    return size;
                } else {
                    System.out.println("Ошибка: " + "Значение должно быть от 1 до 50");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное значение!");
            }
        }
    }

    private boolean chooseDisplayType() {
        while (true) {
            String displayMenu =
                    """
            Выберите тип отображения:
            1. Unicode
            2. Текстовое представление
            """;
            System.out.println(displayMenu);
            System.out.print("➤ Введите выбор (1-2): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return true;
                case "2":
                    return false;
                default:
                    System.out.println("Неверный выбор! Пожалуйста, введите 1 или 2.");
                    System.out.println();
            }
        }
    }

    private void generateMaze(String algorithm, int width, int height, boolean useUnicode) {
        try {
            String algorithmName = getAlgorithmName(algorithm);

            System.out.println("\nГенерация лабиринта с помощью " + algorithmName + "...");

            showLegend(useUnicode);
            System.out.println();

            Generator generator = AlgorithmFactory.createGenerator(algorithm);
            currentMaze = generator.generate(width, height);

            mazePrinter.printMazeToConsole(currentMaze, useUnicode);
            System.out.println("Размер: " + width + "x" + height);
            System.out.println();
            System.out.println("✅ Лабиринт успешно создан!");

            saveToFile(currentMaze);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации лабиринта: " + e.getMessage(), e);
        }
    }

    private void showWelcome() {
        String header = """
            ГЕНЕРАЦИЯ ЛАБИРИНТА
            """;
        System.out.println(header);
    }

    private String showChooseAlgorithm() {
        while (true) {
            String algorithms =
                    """
            Выберите алгоритм генерации:
            1. DFS (поиск в глубину)
            2. Алгоритм Прима
            3. Алгоритм Краскала
            """;
            System.out.println(algorithms);
            System.out.print("➤ Введите выбор (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return "dfs";
                case "2":
                    return "prim";
                case "3":
                    return "kruskal";
                default:
                    System.out.println("Неверный выбор! Пожалуйста, введите 1, 2 или 3.");
                    System.out.println();
            }
        }
    }

    private void saveToFile(Maze maze) {
        String saveMenu = """

        💾 Сохранить лабиринт в файл?
        1. Да
        2. Нет
        """;
        System.out.println(saveMenu);
        System.out.print("➤ Введите выбор (1-2): ");

        String saveChoice = scanner.nextLine().trim();

        if (saveChoice.equals("1")) {
            while (true) {
                System.out.print("📁 Введите имя файла (например, maze.txt): ");
                String fileName = scanner.nextLine().trim();

                if (fileName.isEmpty()) {
                    System.out.println("Имя файла не может быть пустым! Пожалуйста, введите имя файла.");
                    continue;
                }

                if (!fileName.endsWith(".txt")) {
                    fileName += ".txt";
                }

                try {
                    File outputFile = java.nio.file.Path.of(fileName).toFile();
                    mazePrinter.saveMazeToFile(maze, outputFile);
                    System.out.println("✅ Лабиринт сохранен в файл: " + outputFile.getPath());
                    break;
                } catch (Exception e) {
                    System.err.println("Ошибка при сохранении файла: " + e.getMessage());
                    System.out.println("⚠Пожалуйста, попробуйте другое имя файла.");
                }
            }
        } else if (!saveChoice.equals("2")) {
            System.out.println("Неверный выбор, файл не сохранен.");
        }
    }

    private void showLegend(boolean useUnicode) {
        String legend;
        if (useUnicode) {
            legend =
                    """

                ЛЕГЕНДА ЛАБИРИНТА:
                ┌──────────────────────────────────┐
                │ ██ - Стена (непроходимо)         │
                │    - Путь (обычная дорога)       │
                │ ░░ - Грязь (х2 замедление)       │
                │ ▓▓ - Болото (х3 замедление)      │
                └──────────────────────────────────┘
                """;
        } else {
            legend =
                    """

                ЛЕГЕНДА ЛАБИРИНТА:
                ┌──────────────────────────────────┐
                │ #  - Стена (непроходимо)         │
                │    - Путь (обычная дорога)       │
                │ :  - Грязь (х2 замедление)       │
                │ ~  - Болото (х3 замедление)      │
                └──────────────────────────────────┘
                """;
        }
        System.out.println(legend);
    }
}
