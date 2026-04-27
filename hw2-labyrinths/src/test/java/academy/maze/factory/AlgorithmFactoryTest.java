package academy.maze.factory;

import static org.junit.jupiter.api.Assertions.*;

import academy.maze.Generator;
import academy.maze.Solver;
import academy.maze.impl.generator.DFSGenerator;
import academy.maze.impl.generator.KruskalGenerator;
import academy.maze.impl.generator.PrimGenerator;
import academy.maze.impl.solver.AStarSolver;
import academy.maze.impl.solver.BFSSolver;
import academy.maze.impl.solver.DijkstraSolver;
import org.junit.jupiter.api.Test;

class AlgorithmFactoryTest {

    @Test
    void createDFSGenerator() {
        Generator generator = AlgorithmFactory.createGenerator("dfs");
        assertNotNull(generator, "null");
        assertInstanceOf(DFSGenerator.class, generator, "Не тот тип");
    }

    @Test
    void createPrimGenerator() {
        Generator generator = AlgorithmFactory.createGenerator("prim");
        assertNotNull(generator, "null");
        assertInstanceOf(PrimGenerator.class, generator, "Не тот тип");
    }

    @Test
    void createKruskalGenerator() {
        Generator generator = AlgorithmFactory.createGenerator("kruskal");
        assertNotNull(generator, "null");
        assertInstanceOf(KruskalGenerator.class, generator, "Не тот тип");
    }

    @Test
    void createAStarSolver() {
        Solver solver = AlgorithmFactory.createSolver("astar");
        assertNotNull(solver, "null");
        assertInstanceOf(AStarSolver.class, solver, "Не тот тип");
    }

    @Test
    void createDijkstraSolver() {
        Solver solver = AlgorithmFactory.createSolver("dijkstra");
        assertNotNull(solver, "null");
        assertInstanceOf(DijkstraSolver.class, solver, "Не тот тип");
    }

    @Test
    void createBFSSolver() {
        Solver solver = AlgorithmFactory.createSolver("bfs");
        assertNotNull(solver, "null");
        assertInstanceOf(BFSSolver.class, solver, "Не тот тип");
    }

    @Test
    void throwExceptionForUnknownGenerator() {
        assertThrows(
                IllegalArgumentException.class, () -> AlgorithmFactory.createGenerator("unknown"), "Нет исключения");
    }

    @Test
    void throwExceptionForUnknownSolver() {
        assertThrows(IllegalArgumentException.class, () -> AlgorithmFactory.createSolver("unknown"), "Нет исключения");
    }

    @Test
    void handleCaseInsensitiveNames() {
        Generator generator1 = AlgorithmFactory.createGenerator("DFS");
        Generator generator2 = AlgorithmFactory.createGenerator("Dfs");
        assertNotNull(generator1, "null");
        assertNotNull(generator2, "null");
        assertInstanceOf(DFSGenerator.class, generator1, "Не тот тип");
        assertInstanceOf(DFSGenerator.class, generator2, "Не тот тип");
    }
}
