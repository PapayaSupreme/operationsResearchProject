import algorithms.Initialization;
import algorithms.Tools;
import algorithms.GraphAlgo;
import structure.Customer;
import structure.Graph;
import structure.Provision;
import utilities.MenuHelper;
import utilities.Timer;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Graph selectedGraph = null;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("=== Transportation Menu ===");
            System.out.println("Selected graph: " +
                    (selectedGraph == null ? "none" : selectedGraph.getName()));
            System.out.println("1. Graph selection");
            System.out.println("2. Check if it's balanced");
            System.out.println("3. Initial Northwest");
            System.out.println("4. Initial BalasHammer");
            System.out.println("5. Compute total cost for a transportation proposition");
            System.out.println("6. Check if is acyclic");
            System.out.println("7. Check if the problem is connected or not");
            System.out.println("8. Perform one optimization step");
            System.out.println("9. Optimize until optimal solution");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    selectedGraph = MenuHelper.selectGraph(scanner);
                    if (selectedGraph != null) {
                        System.out.println("Loaded graph: " + selectedGraph.getName());
                        System.out.println(selectedGraph);

                        if (!Tools.isBalanced(selectedGraph)) {
                            System.out.println("Note: Graph is unbalanced.");
                        }
                    }
                    break;

                case "2":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println(
                            "Graph is balanced (provisions - orders = 0): "
                                    + Tools.isBalanced(selectedGraph)
                    );
                    break;

                case "3":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println("Computing North-West...");
                    long t0 = System.nanoTime();
                    Initialization.NorthWest(selectedGraph);
                    long t1 = System.nanoTime();
                    Timer.runTimer("North-West initial proposition", t0, t1);
                    System.out.println("Done.");
                    System.out.println(selectedGraph);
                    break;

                case "4":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println("Computing Balas-Hammer...");
                    Initialization.BalasHammer(selectedGraph);
                    System.out.println("Done.");
                    System.out.println(selectedGraph);
                    break;

                case "5":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println("Computing total cost...");
                    Optional<Integer> totalCost = Tools.totalCost(selectedGraph);

                    if (totalCost.isEmpty()) {
                        System.out.println("Make a proposition first.");
                    } else {
                        System.out.println("Total cost: " + totalCost.get());
                    }
                    break;

                case "6":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    if (GraphAlgo.isAcyclic(selectedGraph)) {
                        System.out.println("The transportation proposition is acyclic.");
                    } else {
                        System.out.println("A cycle has been detected.");
                    }
                    break;

                case "7":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    if (GraphAlgo.isConnected(selectedGraph)) {
                        System.out.println("The transportation proposition is connected.");
                    } else {
                        System.out.println("The transportation proposition is not connected.");
                    }
                    break;

                case "8":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    Map.Entry<Provision, Customer> entering =
                            GraphAlgo.findEnteringEdge(selectedGraph);

                    if (entering == null) {
                        System.out.println("Solution is already optimal.");
                        break;
                    }

                    Provision p = entering.getKey();
                    Customer c = entering.getValue();

                    System.out.println("Entering edge: "
                            + p.getName() + " -> " + c.getName());

                    try {
                        java.util.List<Object> cycle =
                                GraphAlgo.buildCycle(selectedGraph, p, c);

                        System.out.println("Cycle found: " + cycle);

                        GraphAlgo.maximizeCycle(selectedGraph, cycle);

                        System.out.println("After optimization:");
                        System.out.println(selectedGraph);

                    } catch (Exception e) {
                        System.out.println("Error during optimization: "
                                + e.getMessage());
                    }
                    break;

                case "9":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    int iteration = 0;

                    while (true) {
                        Map.Entry<Provision, Customer> step =
                                GraphAlgo.findEnteringEdge(selectedGraph);
                        if (!Tools.isBalanced(selectedGraph)) {
                            System.out.println("Note : Graph is unbalanced.");
                        }
                        if (step == null) {
                            System.out.println("Optimal solution reached.");
                            break;
                        }

                        iteration++;

                        Provision pStep = step.getKey();
                        Customer cStep = step.getValue();

                        System.out.println("\nIteration " + iteration);
                        System.out.println("Entering edge: "
                                + pStep.getName() + " -> " + cStep.getName());

                        try {
                            java.util.List<Object> cycle =
                                    GraphAlgo.buildCycle(
                                            selectedGraph,
                                            pStep,
                                            cStep
                                    );

                            GraphAlgo.maximizeCycle(selectedGraph, cycle);

                        } catch (Exception e) {
                            System.out.println("Error during optimization: "
                                    + e.getMessage());
                            break;
                        }
                    }

                    System.out.println("Final solution:");
                    System.out.println(selectedGraph);

                    Optional<Integer> finalCost =
                            Tools.totalCost(selectedGraph);

                    if (finalCost.isPresent()) {
                        System.out.println("Final total cost: "
                                + finalCost.get());
                    }

            }
        }
    }
}