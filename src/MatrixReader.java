import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MatrixReader {

    /**
     * Reads a graph from a file with the specified format and constructs a Graph object.
     * <p>
     * The expected file format is as follows:
     * - The first non-empty line contains two integers: the number of provision vertices and the number of customer vertices.
     * - The next lines correspond to each provision vertex, containing the costs to each customer vertex followed by the provision amount.
     * - After the provision lines, there is a line containing the order amounts for each customer vertex.
     * - The file should not contain any extra non-empty lines after the customer orders line.
     *
     * @param filename the path to the input file
     * @return a Graph object representing the data read from the file
     * @throws IllegalArgumentException if the file format is invalid or if any required data is missing
     * @throws UncheckedIOException if an I/O error occurs while reading the file
     */
    public static Graph read(String filename){
        Path path = Path.of(filename);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = readRequiredLine(reader, "header line with provision/customer counts");
            String[] headerParts = splitLine(headerLine);

            if (headerParts.length != 2) {
                throw new IllegalArgumentException("Header line must contain exactly 2 integers: provisions and customers.");
            }

            int provisionCount = parsePositiveInt(headerParts[0], "number of provision vertices");
            int customerCount = parsePositiveInt(headerParts[1], "number of customer vertices");

            int[][] costs = new int[provisionCount][customerCount];
            int[] provisionAmounts = new int[provisionCount];

            for (int i = 0; i < provisionCount; i++) {
                String provisionLine = readRequiredLine(reader, "provision row " + (i + 1));
                String[] row = splitLine(provisionLine);

                if (row.length != customerCount + 1) {
                    throw new IllegalArgumentException(
                            "Provision row " + (i + 1) + " must contain " + customerCount +
                                    " costs and 1 provision amount."
                    );
                }

                for (int j = 0; j < customerCount; j++) {
                    costs[i][j] = parseNonNegativeInt(row[j], "cost at row " + (i + 1) + ", column " + (j + 1));
                }

                provisionAmounts[i] = parseNonNegativeInt(row[customerCount], "provision amount at row " + (i + 1));
            }

            String customerOrdersLine = readRequiredLine(reader, "customer orders line");
            String[] orderParts = splitLine(customerOrdersLine);

            if (orderParts.length != customerCount) {
                throw new IllegalArgumentException("Customer orders line must contain exactly " + customerCount + " integers.");
            }

            int[] customerOrders = new int[customerCount];
            for (int j = 0; j < customerCount; j++) {
                customerOrders[j] = parseNonNegativeInt(orderParts[j], "order amount for customer " + (j + 1));
            }

            String extraLine = reader.readLine();
            while (extraLine != null && extraLine.trim().isEmpty()) {
                extraLine = reader.readLine();
            }
            if (extraLine != null) {
                throw new IllegalArgumentException("File contains extra non-empty lines after customer orders line.");
            }

            Graph graph = new Graph(path.getFileName().toString());

            Customer[] customers = new Customer[customerCount];
            for (int j = 0; j < customerCount; j++) {
                customers[j] = new Customer("C" + (j + 1), customerOrders[j]);
                graph.addCustomer(customers[j]);
            }

            for (int i = 0; i < provisionCount; i++) {
                Provision provision = new Provision("P" + (i + 1), provisionAmounts[i]);
                for (int j = 0; j < customerCount; j++) {
                    provision.addEdge(costs[i][j], customers[j]);
                }
                graph.addProvision(provision);
            }

            return graph;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read matrix file: " + filename, e);
        }
    }

    /**
     * Reads lines from the provided BufferedReader until it finds a non-empty line, which it returns.
     * If the end of the stream is reached before finding a non-empty line, it throws an IllegalArgumentException.
     *
     * @param reader the BufferedReader to read from
     * @param label a descriptive label for the expected line, used in error messages
     * @return a non-empty line read from the BufferedReader
     * @throws IOException if an I/O error occurs while reading from the BufferedReader
     * @throws IllegalArgumentException if the end of the stream is reached before finding a non-empty line
     */
    private static String readRequiredLine(BufferedReader reader, String label) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("Missing " + label + ".");
            }
            line = line.trim();
        } while (line.isEmpty());

        return line;
    }

    /**
     * Splits a line into tokens, trimming leading and trailing whitespace and using one or more whitespace characters as the delimiter.
     *
     * @param line the line to split
     * @return an array of tokens extracted from the line
     */
    private static String[] splitLine(String line) {
        return line.trim().split("\\s+");
    }

    /**
     * Parses a string token into a positive integer.
     *
     * @param token the string to parse
     * @param label a descriptive label for the value being parsed, used in error messages
     * @return the parsed positive integer value
     * @throws IllegalArgumentException if the token cannot be parsed as a positive integer
     */
    private static int parsePositiveInt(String token, String label) {
        int value = parseInt(token, label);
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid " + label + ": expected a positive integer, got " + value + ".");
        }
        return value;
    }

    /**
     * Parses a string token into a non-negative integer.
     *
     * @param token the string to parse
     * @param label a descriptive label for the value being parsed, used in error messages
     * @return the parsed non-negative integer value
     * @throws IllegalArgumentException if the token cannot be parsed as a non-negative integer
     */
    private static int parseNonNegativeInt(String token, String label) {
        int value = parseInt(token, label);
        if (value < 0) {
            throw new IllegalArgumentException("Invalid " + label + ": expected a non-negative integer, got " + value + ".");
        }
        return value;
    }

    /**
     * Parses a string token into an integer.
     *
     * @param token the string to parse
     * @param label a descriptive label for the value being parsed, used in error messages
     * @return the parsed integer value
     * @throws IllegalArgumentException if the token cannot be parsed as an integer
     */
    private static int parseInt(String token, String label) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + label + ": not an integer (" + token + ").", e);
        }
    }
}
