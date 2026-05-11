package utilities;

import algorithms.Initialization;
import algorithms.GraphAlgo;
import algorithms.Tools;
import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class RandomGraphGenerator {

    /**
     * Generates a randomized transportation graph with n provisions and n customers.
     * The graph is guaranteed to be balanced (total supply = total demand).
     * Costs are random values between minCost and maxCost.
     *
     * @param n the size (number of provisions and customers)
     * @param minCost minimum cost per unit
     * @param maxCost maximum cost per unit
     * @param minSupply minimum supply value
     * @param maxSupply maximum supply value
     * @return a balanced graph with random supplies, demands, and costs
     */
    public static Graph generateRandomGraph(int n, int minCost, int maxCost, int minSupply, int maxSupply) {
        if (n <= 0) {
            throw new IllegalArgumentException("Graph size n must be positive.");
        }
        if (minCost > maxCost) {
            throw new IllegalArgumentException("minCost must be <= maxCost.");
        }
        if (minSupply > maxSupply) {
            throw new IllegalArgumentException("minSupply must be <= maxSupply.");
        }

        Random random = new Random();
        Graph graph = new Graph("Random_" + n + "x" + n);

        // Generate provisions with random supplies
        Provision[] provisions = new Provision[n];
        int totalSupply = 0;
        for (int i = 0; i < n; i++) {
            int supply = minSupply + random.nextInt(maxSupply - minSupply + 1);
            provisions[i] = new Provision("P" + (i + 1), supply);
            graph.addProvision(provisions[i]);
            totalSupply += supply;
        }

        // Generate customers with random demands that sum to totalSupply
        Customer[] customers = new Customer[n];
        int remainingDemand = totalSupply;
        for (int i = 0; i < n - 1; i++) {
            int maxDemand = Math.min(remainingDemand - (n - i - 1), maxSupply);
            int minDemand = Math.max(1, minSupply);
            int demand = minDemand + random.nextInt(Math.max(1, maxDemand - minDemand + 1));
            customers[i] = new Customer("C" + (i + 1), demand);
            graph.addCustomer(customers[i]);
            remainingDemand -= demand;
        }
        // Last customer gets the remaining demand to ensure balance
        customers[n - 1] = new Customer("C" + n, remainingDemand);
        graph.addCustomer(customers[n - 1]);

        // Generate random costs for all routes
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int cost = minCost + random.nextInt(maxCost - minCost + 1);
                provisions[i].addEdge(customers[j], cost);
            }
        }

        return graph;
    }

    /**
     * Generates a random graph and measures the execution time of both NorthWest and BalasHammer algorithms,
     * plus their stepping-stone optimization. Prints a comprehensive comparison report to the console.
     *
     * @param n the size of the graph
     */
    public static void benchmarkAlgorithms(int n) {
        System.out.println("\n=== Generating Random Graph ===");
        System.out.println("Size: " + n + "x" + n + " (" + (n * n) + " routes)");

        long t0 = System.nanoTime();
        Graph graph1 = generateRandomGraph(n, 1, 100, 1, 100);
        long t1 = System.nanoTime();
        Timer.runTimer("Random graph generation", t0, t1);

        // Make a deep copy of the graph for the second algorithm
        Graph graph2 = copyGraph(graph1);

        // Test NorthWest initialization
        System.out.println("\n--- Testing NorthWest Algorithm ---");
        t0 = System.nanoTime();
        Initialization.NorthWest(graph1);
        t1 = System.nanoTime();
        double nwInitTime = (t1 - t0) / 1000000.0;
        Timer.runTimer("NorthWest initialization", t0, t1);

        // Get initial cost for NW
        Optional<Integer> nwInitCost = Tools.totalCost(graph1);
        System.out.println("Initial cost after NorthWest: " + (nwInitCost.isPresent() ? nwInitCost.get() : "N/A"));

        // Run stepping-stone optimization on NorthWest solution
        System.out.println("Optimizing with Stepping-Stone...");
        t0 = System.nanoTime();
        int nwIterations = runSteppingStoneOptimization(graph1);
        t1 = System.nanoTime();
        double nwOptTime = (t1 - t0) / 1000000.0;
        Timer.runTimer("NorthWest stepping-stone optimization", t0, t1);

        Optional<Integer> nwFinalCost = Tools.totalCost(graph1);
        System.out.println("Final cost after NorthWest optimization: " + (nwFinalCost.isPresent() ? nwFinalCost.get() : "N/A"));
        System.out.println("Optimization iterations: " + nwIterations);

        // Test BalasHammer initialization
        System.out.println("\n--- Testing BalasHammer Algorithm ---");
        t0 = System.nanoTime();
        Initialization.BalasHammer(graph2);
        t1 = System.nanoTime();
        double bhInitTime = (t1 - t0) / 1000000.0;
        Timer.runTimer("BalasHammer initialization", t0, t1);

        // Get initial cost for BH
        Optional<Integer> bhInitCost = Tools.totalCost(graph2);
        System.out.println("Initial cost after BalasHammer: " + (bhInitCost.isPresent() ? bhInitCost.get() : "N/A"));

        // Run stepping-stone optimization on BalasHammer solution
        System.out.println("Optimizing with Stepping-Stone...");
        t0 = System.nanoTime();
        int bhIterations = runSteppingStoneOptimization(graph2);
        t1 = System.nanoTime();
        double bhOptTime = (t1 - t0) / 1000000.0;
        Timer.runTimer("BalasHammer stepping-stone optimization", t0, t1);

        Optional<Integer> bhFinalCost = Tools.totalCost(graph2);
        System.out.println("Final cost after BalasHammer optimization: " + (bhFinalCost.isPresent() ? bhFinalCost.get() : "N/A"));
        System.out.println("Optimization iterations: " + bhIterations);

        // Print comprehensive comparison
        System.out.println("\n=== Comprehensive Performance Comparison ===");
        System.out.println("\nInitialization Phase:");
        System.out.println("  NorthWest:    " + String.format("%.3f", nwInitTime) + " ms");
        System.out.println("  BalasHammer:  " + String.format("%.3f", bhInitTime) + " ms");
        if (nwInitTime < bhInitTime) {
            System.out.println("  Winner: NorthWest is faster by " + String.format("%.2f", (bhInitTime - nwInitTime) / nwInitTime * 100) + "%");
        } else {
            System.out.println("  Winner: BalasHammer is faster by " + String.format("%.2f", (nwInitTime - bhInitTime) / bhInitTime * 100) + "%");
        }

        System.out.println("\nOptimization Phase:");
        System.out.println("  NorthWest+SS:    " + String.format("%.3f", nwOptTime) + " ms (" + nwIterations + " iterations)");
        System.out.println("  BalasHammer+SS:  " + String.format("%.3f", bhOptTime) + " ms (" + bhIterations + " iterations)");
        if (nwOptTime < bhOptTime) {
            System.out.println("  Winner: NorthWest is faster by " + String.format("%.2f", (bhOptTime - nwOptTime) / nwOptTime * 100) + "%");
        } else {
            System.out.println("  Winner: BalasHammer is faster by " + String.format("%.2f", (nwOptTime - bhOptTime) / bhOptTime * 100) + "%");
        }

        System.out.println("\nTotal Time (Init + Optimization):");
        double nwTotal = nwInitTime + nwOptTime;
        double bhTotal = bhInitTime + bhOptTime;
        System.out.println("  NorthWest:    " + String.format("%.3f", nwTotal) + " ms");
        System.out.println("  BalasHammer:  " + String.format("%.3f", bhTotal) + " ms");
        if (nwTotal < bhTotal) {
            System.out.println("  Winner: NorthWest is faster by " + String.format("%.2f", (bhTotal - nwTotal) / nwTotal * 100) + "%");
        } else {
            System.out.println("  Winner: BalasHammer is faster by " + String.format("%.2f", (nwTotal - bhTotal) / bhTotal * 100) + "%");
        }

        System.out.println("\nFinal Solution Quality:");
        System.out.println("  NorthWest final cost:    " + (nwFinalCost.isPresent() ? nwFinalCost.get() : "N/A"));
        System.out.println("  BalasHammer final cost:  " + (bhFinalCost.isPresent() ? bhFinalCost.get() : "N/A"));
        if (nwFinalCost.isPresent() && bhFinalCost.isPresent()) {
            if (nwFinalCost.get() < bhFinalCost.get()) {
                System.out.println("  Winner: NorthWest has lower cost by " + (bhFinalCost.get() - nwFinalCost.get()) + " units");
            } else if (bhFinalCost.get() < nwFinalCost.get()) {
                System.out.println("  Winner: BalasHammer has lower cost by " + (nwFinalCost.get() - bhFinalCost.get()) + " units");
            } else {
                System.out.println("  Both methods achieved the same final cost.");
            }
        }
    }

    /**
     * Runs the stepping-stone algorithm until an optimal solution is found.
     * Returns the number of iterations performed.
     *
     * @param graph the graph with an initial feasible solution
     * @return the number of optimization iterations
     */
    private static int runSteppingStoneOptimization(Graph graph) {
        int iteration = 0;
        while (true) {
            Map.Entry<Provision, Customer> entering = GraphAlgo.findEnteringEdge(graph);
            if (entering == null) {
                break;
            }

            iteration++;
            Provision p = entering.getKey();
            Customer c = entering.getValue();

            try {
                List<Object> cycle = GraphAlgo.buildCycle(graph, p, c);
                GraphAlgo.maximizeCycle(graph, cycle);
            } catch (Exception e) {
                // Stop optimization if an error occurs
                break;
            }
        }
        return iteration;
    }

    /**
     * Creates a deep copy of a graph (including all provisions, customers, and edges).
     * Used to test multiple algorithms on the same initial data.
     *
     * @param original the graph to copy
     * @return a new independent copy of the graph
     */
    private static Graph copyGraph(Graph original) {
        Graph copy = new Graph(original.getName() + "_copy");

        // Map original vertices to copied vertices to maintain relationships
        Map<Provision, Provision> provisionMap = new HashMap<>();
        Map<Customer, Customer> customerMap = new HashMap<>();

        // Copy provisions
        for (Provision originalP : original.getProvisions().values()) {
            Provision copyP = new Provision(originalP.getName(), originalP.getProvision());
            copy.addProvision(copyP);
            provisionMap.put(originalP, copyP);
        }

        // Copy customers
        for (Customer originalC : original.getCustomers().values()) {
            Customer copyC = new Customer(originalC.getName(), originalC.getOrder());
            copy.addCustomer(copyC);
            customerMap.put(originalC, copyC);
        }

        // Add edges (costs) from original to copy using the mapped vertices
        for (Provision originalP : original.getProvisions().values()) {
            Provision copyP = provisionMap.get(originalP);
            for (Customer originalC : original.getCustomers().values()) {
                Customer copyC = customerMap.get(originalC);
                Integer cost = originalP.getCosts().get(originalC);
                if (cost != null && copyP != null && copyC != null) {
                    copyP.addEdge(copyC, cost);
                }
            }
        }

        return copy;
    }
}

