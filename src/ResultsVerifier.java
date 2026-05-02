import algorithms.GraphAlgo;
import algorithms.Initialization;
import algorithms.MatrixReader;
import algorithms.Tools;
import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies that the transportation problem solver produces correct results.
 */
public class ResultsVerifier {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("TRANSPORTATION PROBLEM SOLVER - RESULTS VERIFICATION");
        System.out.println("=".repeat(80));
        System.out.println();
        
        int totalTests = 0;
        int passedTests = 0;
        
        for (int problemNum = 1; problemNum <= 12; problemNum++) {
            System.out.println("\n" + "-".repeat(80));
            System.out.println("PROBLEM " + problemNum);
            System.out.println("-".repeat(80));
            
            boolean[] results = testProblem(problemNum);
            totalTests += 2;
            if (results[0]) passedTests++;
            if (results[1]) passedTests++;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("VERIFICATION COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Tests passed: " + passedTests + "/" + totalTests);
        
        if (passedTests == totalTests) {
            System.out.println("\n✅ ALL TESTS PASSED! Your algorithm is working correctly!");
        } else {
            System.out.println("\n⚠️  Some tests failed. Please review the errors above.");
        }
    }
    
    private static boolean[] testProblem(int problemNum) {
        boolean nwPassed = false;
        boolean bhPassed = false;
        
        try {
            String filename = "src/problems/problem" + problemNum + ".txt";
            File file = new File(filename);
            
            if (!file.exists()) {
                System.out.println("❌ File not found: " + filename);
                return new boolean[]{false, false};
            }
            
            Graph graphNW = MatrixReader.read(file.getAbsolutePath());
            System.out.println("\n--- North-West Method ---");
            nwPassed = testMethod(graphNW, "NW");
            
            Graph graphBH = MatrixReader.read(file.getAbsolutePath());
            System.out.println("\n--- Balas-Hammer Method ---");
            bhPassed = testMethod(graphBH, "BH");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        return new boolean[]{nwPassed, bhPassed};
    }
    
    private static boolean testMethod(Graph graph, String method) {
        try {
            boolean balanced = Tools.isBalanced(graph);
            System.out.println("Balanced: " + (balanced ? "✓" : "✗"));
            
            if (method.equals("NW")) {
                Initialization.NorthWest(graph);
            } else {
                Initialization.BalasHammer(graph);
            }
            
            Optional<Integer> initialCost = Tools.totalCost(graph);
            System.out.println("Initial cost: " + 
                (initialCost.isPresent() ? initialCost.get() : "N/A"));
            
            int iterations = 0;
            int maxIterations = 100;
            
            while (iterations < maxIterations) {
                Map.Entry<Provision, Customer> entering = GraphAlgo.findEnteringEdge(graph);
                
                if (entering == null) {
                    break;
                }
                
                iterations++;
                
                try {
                    java.util.List<Object> cycle = GraphAlgo.buildCycle(
                        graph, 
                        entering.getKey(), 
                        entering.getValue()
                    );
                    GraphAlgo.maximizeCycle(graph, cycle);
                } catch (Exception e) {
                    System.out.println("❌ Optimization failed: " + e.getMessage());
                    return false;
                }
            }
            
            Optional<Integer> finalCost = Tools.totalCost(graph);
            System.out.println("Final cost: " + 
                (finalCost.isPresent() ? finalCost.get() : "N/A"));
            System.out.println("Iterations: " + iterations);
            
            boolean optimal = GraphAlgo.isOptimal(graph);
            System.out.println("Is optimal: " + (optimal ? "✓" : "✗"));
            
            boolean feasible = checkFeasibility(graph);
            System.out.println("Is feasible: " + (feasible ? "✓" : "✗"));
            
            boolean acyclic = GraphAlgo.isAcyclic(graph);
            System.out.println("Is acyclic: " + (acyclic ? "✓" : "✗"));
            
            boolean connected = GraphAlgo.isConnected(graph);
            System.out.println("Is connected: " + (connected ? "✓" : "✗"));
            
            int basicVars = countBasicVariables(graph);
            int expected = graph.getProvisions().size() + graph.getCustomers().size() - 1;
            boolean correctBasicVars = (basicVars == expected);
            System.out.println("Basic variables: " + basicVars + 
                (correctBasicVars ? " ✓" : " ⚠ (expected " + expected + ")"));
            
            boolean potentialsOk = true;
            if (optimal) {
                potentialsOk = verifyPotentials(graph);
                System.out.println("Potentials valid: " + (potentialsOk ? "✓" : "✗"));
            }
            
            if (optimal && feasible && acyclic && connected && potentialsOk) {
                System.out.println("✅ SOLUTION IS VALID AND OPTIMAL");
                return true;
            } else {
                System.out.println("⚠️  WARNING: Solution has issues");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERROR in " + method + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean checkFeasibility(Graph graph) {
        for (Provision p : graph.getProvisions().values()) {
            int shipped = 0;
            for (Integer quantity : p.getShippings().values()) {
                shipped += quantity;
            }
            if (shipped != p.getProvision()) {
                System.out.println("  ⚠ " + p.getName() + " provision not satisfied: " 
                    + shipped + " != " + p.getProvision());
                return false;
            }
        }
        
        for (Customer c : graph.getCustomers().values()) {
            int received = 0;
            for (Provision p : graph.getProvisions().values()) {
                Integer quantity = p.getShippings().get(c);
                if (quantity != null) {
                    received += quantity;
                }
            }
            if (received != c.getOrder()) {
                System.out.println("  ⚠ " + c.getName() + " order not satisfied: " 
                    + received + " != " + c.getOrder());
                return false;
            }
        }
        
        return true;
    }
    
    private static int countBasicVariables(Graph graph) {
        int count = 0;
        for (Provision p : graph.getProvisions().values()) {
            for (Integer quantity : p.getShippings().values()) {
                if (quantity > 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private static boolean verifyPotentials(Graph graph) {
        try {
            Map<Object, Integer> potentials = GraphAlgo.computePotentials(graph);
            
            for (Provision p : graph.getProvisions().values()) {
                for (Map.Entry<Customer, Integer> entry : p.getShippings().entrySet()) {
                    Customer c = entry.getKey();
                    
                    if (entry.getValue() > 0) {
                        int ui = potentials.getOrDefault(p, 0);
                        int vj = potentials.getOrDefault(c, 0);
                        int aij = p.getCosts().get(c);
                        
                        if (ui + vj != aij) {
                            System.out.println("  ⚠ Potential error at " + p.getName() 
                                + "->" + c.getName() + ": u + v = " + (ui + vj) 
                                + " != " + aij);
                            return false;
                        }
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("  ⚠ Error verifying potentials: " + e.getMessage());
            return false;
        }
    }
}
