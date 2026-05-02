import algorithms.GraphAlgo;
import algorithms.Initialization;
import algorithms.Tools;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import structure.Customer;
import structure.Graph;
import structure.Provision;
import utilities.MenuHelper;
import utilities.Timer;

public class Main {

    public static void main(String[] args) {
        Graph selectedGraph = null;
        Scanner scanner = new Scanner(System.in);
        boolean end = false;
        while (!end) {
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
            long t0, t1;
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

                    t0 = System.nanoTime();
                    boolean isBalanced = Tools.isBalanced(selectedGraph);
                    t1 = System.nanoTime();
                    Timer.runTimer("Balance check", t0, t1);
                    System.out.println(
                            "Graph is balanced (provisions - orders = 0): "
                                    + isBalanced
                    );
                    break;

                case "3":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println("Computing North-West...");
                    t0 = System.nanoTime();
                    Initialization.NorthWest(selectedGraph);
                    GraphAlgo.ensureConnected(selectedGraph);  // ✅ ADD THIS LINE
                    t1 = System.nanoTime();
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
                    t0 = System.nanoTime();
                    Initialization.BalasHammer(selectedGraph);
                    GraphAlgo.ensureConnected(selectedGraph);  // ✅ ADD THIS LINE
                    t1 = System.nanoTime();
                    Timer.runTimer("Balas-Hammer initial proposition", t0, t1);
                    System.out.println("Done.");
                    System.out.println(selectedGraph);
                    break;
                case "5":
                    if (selectedGraph == null) {
                        System.out.println("Please select a graph first.");
                        break;
                    }

                    System.out.println("Computing total cost...");
                    t0 = System.nanoTime();
                    Optional<Integer> totalCost = Tools.totalCost(selectedGraph);
                    t1 = System.nanoTime();
                    Timer.runTimer("Total cost compute", t0, t1);
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

                    t0 = System.nanoTime();
                    boolean isAcyclic = GraphAlgo.isAcyclic(selectedGraph);
                    t1 = System.nanoTime();
                    Timer.runTimer("Acyclic check", t0, t1);
                    if (isAcyclic) {
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

                    t0 = System.nanoTime();
                    boolean isConnected = GraphAlgo.isConnected(selectedGraph);
                    t1 = System.nanoTime();
                    Timer.runTimer("Connected check", t0, t1);

                    if (isConnected) {
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

                    t0 = System.nanoTime();
                    Map.Entry<Provision, Customer> entering =
                            GraphAlgo.findEnteringEdge(selectedGraph);

                    if (entering == null) {
                        t1 = System.nanoTime();
                        Timer.runTimer("One optimization iteration", t0, t1);
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

                        t1 = System.nanoTime();
                        Timer.runTimer("One optimization iteration", t0, t1);

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

                    t0 = System.nanoTime();
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

                    t1 = System.nanoTime();
                    Timer.runTimer("Transportation proposition optimization", t0, t1);

                    System.out.println("Final solution:");
                    System.out.println(selectedGraph);

                    Optional<Integer> finalCost =
                            Tools.totalCost(selectedGraph);

                    finalCost.ifPresent(integer -> System.out.println("Final total cost: "
                            + integer));
                    break;

                case "0":
                    end = true;
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Choose a valid option.");
                    break;
            }
        }
    }
}