import algorithms.MatrixReader;
import java.io.File;
import java.nio.file.Path;

import structure.Graph;
import utilities.TraceGenerator;

/**
 * Batch generator for all 24 execution traces.
 * Runs all 12 problems with both NW and BH initialization methods.
 */
public class BatchTraceGenerator {
    
    public static void main(String[] args) {
        int groupNumber = 2;
        int teamNumber = 2;
        
        System.out.println("=".repeat(80));
        System.out.println("BATCH TRACE GENERATION");
        System.out.println("Group: " + groupNumber + ", Team: " + teamNumber);
        System.out.println("=".repeat(80));
        System.out.println();
        
        String[] methods = {"NW", "BH"};
        int successCount = 0;
        int failCount = 0;
        
        for (int problemNum = 1; problemNum <= 12; problemNum++) {
            for (String method : methods) {
                System.out.println("\n" + "=".repeat(80));
                System.out.println("Processing: Problem " + problemNum + " with " + method);
                System.out.println("=".repeat(80));
                
                try {
                    Path problemsDir = Path.of("src", "problems");
                    String filename = problemsDir.resolve("problem" + problemNum + ".txt").toString();
                    File file = new File(filename);
                    
                    if (!file.exists()) {
                        System.err.println("ERROR: Problem file not found: " + filename);
                        failCount++;
                        continue;
                    }
                    
                    Graph graph = MatrixReader.read(file.getAbsolutePath());

                    TraceGenerator.generateTrace(graph, method, groupNumber, teamNumber, problemNum);
                    
                    successCount++;
                    System.out.println("✓ Successfully generated trace for Problem " + problemNum + " - " + method);
                    
                } catch (Exception e) {
                    System.err.println("ERROR processing Problem " + problemNum + " with " + method);
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                    failCount++;
                }
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BATCH GENERATION COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Successfully generated: " + successCount + "/24 traces");
        System.out.println("Failed: " + failCount + "/24 traces");
        
        if (successCount == 24) {
            System.out.println("\n🎉 All traces generated successfully!");
            System.out.println("\nFiles created:");
            for (int i = 1; i <= 12; i++) {
                System.out.println("  " + groupNumber + "-" + teamNumber + "-trace" + i + "-nw.txt");
                System.out.println("  " + groupNumber + "-" + teamNumber + "-trace" + i + "-bh.txt");
            }
        } else {
            System.out.println("\n⚠ Warning: Some traces failed to generate.");
            System.out.println("Please check the error messages above.");
        }
    }
}
