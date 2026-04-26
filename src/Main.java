import algorithms.Initialization;
import algorithms.Tools;
import structure.Graph;
import utilities.MenuHelper;

import java.util.Scanner;


void main() {
    Graph selectedGraph = null;
    Scanner scanner = new Scanner(System.in);

    while (true) {
        System.out.println();
        System.out.println("=== Transportation Menu ===");
        System.out.println("Selected graph: " + (selectedGraph == null ? "none" : selectedGraph.getName()));
        System.out.println("1. Graph selection");
        System.out.println("2. Check if it's balanced");
        System.out.println("3. Initial Northwest");
        System.out.println("4. Initial BalasHammer");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                selectedGraph = MenuHelper.selectGraph(scanner);
                if (selectedGraph != null) {
                    System.out.println("Loaded graph: " + selectedGraph.getName());
                    System.out.println(selectedGraph);
                }
                break;
            case "2":
                System.out.println("Graph is balanced (provisions - orders = 0) : " + Tools.isBalanced(selectedGraph));
                break;
            case "3":
                System.out.println("Computing North-West...");
                Initialization.NorthWest(selectedGraph);
                System.out.println("Done.");
                System.out.println(selectedGraph);
                break;
            case "4":
                System.out.println("Computing Balas-Hammer...");
                Initialization.BalasHammer(selectedGraph);
                System.out.println("Done.");
                System.out.println(selectedGraph);
                break;
            case "5":
                System.out.println("Exiting...");
                return;
            default:
                System.out.println("Invalid option. Please choose 1 to 5.");
        }
    }
}


