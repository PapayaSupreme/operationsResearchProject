package utilities;

import algorithms.MatrixReader;
import algorithms.Tools;
import structure.Graph;

import java.nio.file.Path;

public class CostMatrixViewer {
    private static final Path PROBLEM_FILE = Path.of("src", "problems", "problem5.txt"); //change here the hardcoded problem path

    public static void display() {
        Graph graph = MatrixReader.read(PROBLEM_FILE.toString());
        System.out.println(graph);

        System.out.println("\n--- Cost Matrix (without dummy customer) ---");
        int[][] costMatrix = Tools.buildCostMatrix(
                graph.getProvisions().values().stream().toList(),
                graph.getCustomers().values().stream().toList(),
                false
        );
        displayMatrix(costMatrix);

        System.out.println("\n--- Cost Matrix (with dummy customer) ---");
        int[][] costMatrixWithDummy = Tools.buildCostMatrix(
                graph.getProvisions().values().stream().toList(),
                graph.getCustomers().values().stream().toList(),
                true
        );
        displayMatrix(costMatrixWithDummy);
    }

    private static void displayMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        display();
    }
}

