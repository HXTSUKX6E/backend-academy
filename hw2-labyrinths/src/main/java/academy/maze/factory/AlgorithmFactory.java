package academy.maze.factory;

import academy.maze.Generator;
import academy.maze.Solver;
import academy.maze.impl.generator.DFSGenerator;
import academy.maze.impl.generator.KruskalGenerator;
import academy.maze.impl.generator.PrimGenerator;
import academy.maze.impl.solver.AStarSolver;
import academy.maze.impl.solver.BFSSolver;
import academy.maze.impl.solver.DijkstraSolver;

public class AlgorithmFactory {

    public static Generator createGenerator(String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "dfs" -> new DFSGenerator();
            case "prim" -> new PrimGenerator();
            case "kruskal" -> new KruskalGenerator();
            default -> throw new IllegalArgumentException("Unknown generator algorithm: " + algorithm);
        };
    }

    public static Solver createSolver(String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "astar" -> new AStarSolver();
            case "dijkstra" -> new DijkstraSolver();
            case "bfs" -> new BFSSolver();
            default -> throw new IllegalArgumentException("Unknown solver algorithm: " + algorithm);
        };
    }
}
