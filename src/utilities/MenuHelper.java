package utilities;

import algorithms.MatrixReader;
import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuHelper {
    public static Graph selectGraph(Scanner scanner) {
        Path problemsDir = Path.of("src", "problems");

        List<Path> problemFiles;
        try (Stream<Path> pathStream = Files.list(problemsDir)) {
            problemFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .toList();
        } catch (IOException e) {
            System.out.println("Could not list problems directory: " + problemsDir);
            return null;
        }

        // Ensure we have a mutable list (Stream.toList may return an unmodifiable list) before sorting
        problemFiles = new java.util.ArrayList<>(problemFiles);

        // Sort using numeric-aware comparator: extract first integer from filename (e.g. "problem11.txt" -> 11)
        problemFiles.sort(Comparator.comparingInt(MenuHelper::extractProblemNumber)
                .thenComparing(path -> path.getFileName().toString()));

        if (problemFiles.isEmpty()) {
            System.out.println("No .txt problem files found in: " + problemsDir);
            return null;
        }

        System.out.println("Available problem files:");
        for (int i = 0; i < problemFiles.size(); i++) {
            System.out.println((i + 1) + ". " + problemFiles.get(i).getFileName());
        }
        System.out.print("Select file number: ");

        String input = scanner.nextLine().trim();
        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return null;
        }

        if (index < 0 || index >= problemFiles.size()) {
            System.out.println("Selection out of range.");
            return null;
        }

        Path selectedPath = problemFiles.get(index);
        try {
            return MatrixReader.read(selectedPath.toString());
        } catch (RuntimeException e) {
            System.out.println("Failed to load graph from " + selectedPath.getFileName() + ": " + e.getMessage());
            return null;
        }
    }

    // Extract the first integer found in the filename. If none found, return Integer.MAX_VALUE
    private static int extractProblemNumber(Path path) {
        String name = path.getFileName().toString();
        Matcher m = Pattern.compile("(\\d+)").matcher(name);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                // fall through to return MAX
            }
        }
        return Integer.MAX_VALUE;
    }

    public static class ConsoleColors {
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
    }
}
