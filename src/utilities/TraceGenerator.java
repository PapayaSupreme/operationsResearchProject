package utilities;

import algorithms.GraphAlgo;
import algorithms.Initialization;
import algorithms.Tools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import structure.Customer;
import structure.Graph;
import structure.Provision;

/**
 * Utility class to generate execution traces for transportation problems.
 */
public class TraceGenerator {
    
    private PrintStream fileOutput;
    private PrintStream consoleOutput;
    private boolean tracing = false;
    
    /**
     * Start tracing to a file.
     */
    public void startTrace(String filename) {
        try {
            consoleOutput = System.out;
            fileOutput = new PrintStream(new FileOutputStream(filename));
            
            System.setOut(new DualPrintStream(consoleOutput, fileOutput));
            
            tracing = true;
            
            println("=".repeat(80));
            println("EXECUTION TRACE - " + filename);
            println("Generated: " + java.time.LocalDateTime.now());
            println("=".repeat(80));
            println();
            
        } catch (Exception e) {
            System.err.println("Error starting trace: " + e.getMessage());
            tracing = false;
        }
    }
    
    /**
     * Stop tracing and close the file.
     */
    public void stopTrace() {
        if (tracing) {
            println();
            println("=".repeat(80));
            println("END OF TRACE");
            println("=".repeat(80));
            
            System.setOut(consoleOutput);
            
            if (fileOutput != null) {
                fileOutput.close();
            }
            
            tracing = false;
        }
    }
    
    private void println(String message) {
        System.out.println(message);
    }
    
    private void println() {
        System.out.println();
    }
    
    /**
     * Run a complete trace for a problem with initialization method.
     */
    public static void generateTrace(
            Graph graph, 
            String method, 
            int groupNumber, 
            int teamNumber, 
            int problemNumber) {
        
        TraceGenerator trace = new TraceGenerator();
        
        String methodLower = method.equalsIgnoreCase("NW") ? "nw" : "bh";
        String filename = String.format("%d-%d-trace%d-%s.txt", 
                                        groupNumber, teamNumber, problemNumber, methodLower);
        
        // Create traces directory if it doesn't exist
        File traceDir = new File("src/traces");
        if (!traceDir.exists()) {
            traceDir.mkdirs();
        }

        String tracePath = "src/traces/" + filename;
        trace.startTrace(tracePath);

        try {
            System.out.println("PROBLEM " + problemNumber);
            System.out.println("Initialization Method: " + method);
            System.out.println();
            
            System.out.println("--- INITIAL PROBLEM ---");
            System.out.println(graph);
            System.out.println();
            
            boolean balanced = Tools.isBalanced(graph);
            System.out.println("Graph is balanced: " + balanced);
            System.out.println();
            
           System.out.println("--- INITIALIZATION: " + method + " ---");
            if (method.equalsIgnoreCase("NW")) {
                Initialization.NorthWest(graph);
            } else {
                Initialization.BalasHammer(graph);
            }
            GraphAlgo.isConnected(graph);  
            System.out.println(graph);
            
            Optional<Integer> initialCost = Tools.totalCost(graph);
            initialCost.ifPresent(integer -> System.out.println("Initial total cost: " + integer));
            System.out.println();
            
            System.out.println("--- INITIAL SOLUTION PROPERTIES ---");
            System.out.println("Is acyclic: " + GraphAlgo.isAcyclic(graph));
            System.out.println("Is connected: " + GraphAlgo.isConnectedIncludingZeroFlow(graph));
            System.out.println("Is optimal: " + GraphAlgo.isOptimal(graph));
            System.out.println();
            
            System.out.println("--- STEPPING-STONE OPTIMIZATION ---");
            System.out.println();
            
            int iteration = 0;
            int maxIterations = 100;
            
            while (iteration < maxIterations) {
                Map.Entry<Provision, Customer> entering = GraphAlgo.findEnteringEdge(graph);
                
                if (entering == null) {
                    System.out.println("*** OPTIMAL SOLUTION REACHED ***");
                    break;
                }
                
                iteration++;
                Provision p = entering.getKey();
                Customer c = entering.getValue();
                
                System.out.println("--- Iteration " + iteration + " ---");
                System.out.println("Entering edge: " + p.getName() + " -> " + c.getName());
                
                Map<Object, Integer> potentials = GraphAlgo.computePotentials(graph);
                System.out.println("Potentials:");
                for (Provision prov : graph.getProvisions().values()) {
                    if (potentials.containsKey(prov)) {
                        System.out.println("  u[" + prov.getName() + "] = " + potentials.get(prov));
                    }
                }
                for (Customer cust : graph.getCustomers().values()) {
                    if (potentials.containsKey(cust)) {
                        System.out.println("  v[" + cust.getName() + "] = " + potentials.get(cust));
                    }
                }
                
                try {
                    List<Object> cycle = GraphAlgo.buildCycle(graph, p, c);
                    System.out.println("Cycle: " + formatCycle(cycle));
                    
                    GraphAlgo.maximizeCycle(graph, cycle);
                    
                    System.out.println("Updated transportation:");
                    System.out.println(graph);
                    
                    Optional<Integer> currentCost = Tools.totalCost(graph);
                    currentCost.ifPresent(integer -> System.out.println("Current total cost: " + integer));
                    
                } catch (Exception e) {
                    System.out.println("Error during optimization: " + e.getMessage());
                    break;
                }
                
                System.out.println();
            }
            
            if (iteration >= maxIterations) {
                System.out.println("*** WARNING: Maximum iterations reached ***");
            }
            
            System.out.println("=".repeat(80));
            System.out.println("--- FINAL OPTIMAL SOLUTION ---");
            System.out.println(graph);
            
            Optional<Integer> finalCost = Tools.totalCost(graph);
            if (finalCost.isPresent()) {
                System.out.println();
                System.out.println("FINAL TOTAL COST: " + finalCost.get());
            }
            
            System.out.println();
            System.out.println("Total iterations: " + iteration);
            System.out.println("=".repeat(80));
            
        } finally {
            trace.stopTrace();
        }
        
        System.out.println("Trace saved to: src/traces/" + filename);
    }
    
    private static String formatCycle(List<Object> cycle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cycle.size(); i++) {
            Object vertex = cycle.get(i);
            String name = "";
            
            if (vertex instanceof Provision) {
                name = ((Provision) vertex).getName();
            } else if (vertex instanceof Customer) {
                name = ((Customer) vertex).getName();
            }
            
            sb.append(name);
            
            if (i < cycle.size() - 1) {
                sb.append(i % 2 == 0 ? " --(+)--> " : " --(−)--> ");
            }
        }
        return sb.toString();
    }
    
    /**
     * Helper class to duplicate output to both console and file.
     */
    private static class DualPrintStream extends PrintStream {
        private PrintStream second;
        
        public DualPrintStream(PrintStream first, PrintStream second) {
            super(first);
            this.second = second;
        }
        
        @Override
        public void write(byte[] buf, int off, int len) {
            super.write(buf, off, len);
            second.write(buf, off, len);
        }
        
        @Override
        public void flush() {
            super.flush();
            second.flush();
        }
        
        @Override
        public void close() {
            super.close();
            second.close();
        }
    }
}
