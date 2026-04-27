package academy.maze.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractiveModeTest {

    private InteractiveMode interactiveMode;

    @BeforeEach
    void setUp() {
        interactiveMode = new InteractiveMode(null);
    }

    @Test
    void getAlgorithmName_DFS_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "dfs");
        assertEquals("DFS (поиск в глубину)", result);
    }

    @Test
    void getAlgorithmName_Prim_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "prim");
        assertEquals("Алгоритм Прима", result);
    }

    @Test
    void getAlgorithmName_Kruskal_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "kruskal");
        assertEquals("Алгоритм Краскала", result);
    }

    @Test
    void getSolverAlgorithmName_AStar_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getSolverAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "astar");
        assertEquals("A*", result);
    }

    @Test
    void getSolverAlgorithmName_Dijkstra_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getSolverAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "dijkstra");
        assertEquals("Дейкстра", result);
    }

    @Test
    void getSolverAlgorithmName_BFS_ReturnsCorrectName() throws Exception {
        Method method = InteractiveMode.class.getDeclaredMethod("getSolverAlgorithmName", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(interactiveMode, "bfs");
        assertEquals("BFS", result);
    }
}
