package utilities;

import algorithms.MatrixReader;
import structure.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class MenuHelper {
    public static Graph selectGraph(Scanner scanner) {
        Path problemsDir = Path.of("src", "problems");

        List<Path> problemFiles;
        try (Stream<Path> pathStream = Files.list(problemsDir)) {
            problemFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            System.out.println("Could not list problems directory: " + problemsDir);
            return null;
        }

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
}
