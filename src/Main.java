import algorithms.Initialization;
import algorithms.Tools;
import structure.Graph;
import utilities.MenuHelper;
import utilities.Timer;

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
        System.out.println("5. Compute total cost for a transportation proposition");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                selectedGraph = MenuHelper.selectGraph(scanner);
                if (selectedGraph != null) {
                    System.out.println("Loaded graph: " + selectedGraph.getName());
                    System.out.println(selectedGraph);
                    if (!Tools.isBalanced(selectedGraph)){
                        System.out.println("Note : Graph is unbalanced.");
                    }
                }
                break;
            case "2":
                System.out.println("Graph is balanced (provisions - orders = 0) : " + Tools.isBalanced(selectedGraph));
                break;
            case "3":
                System.out.println("Computing North-West...");
                long t0 = System.nanoTime();
                Initialization.NorthWest(selectedGraph);
                long t1 = System.nanoTime();
                Timer.runTimer("North-West", t0, t1);
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
                System.out.println("Computing total cost for this transportation proposition...");
                Optional<Integer> totalCost = Tools.totalCost(selectedGraph);
                System.out.println("Done.");
                if (totalCost.isEmpty()){
                    System.out.println("Make a proposition first.");
                }
                else{
                    System.out.println("Total cost for this proposition: " + totalCost.get());
                }
                break;
            case "0":
                System.out.println("Exiting...");
                return;
            default:
                System.out.println("Invalid option. Please choose 0 to 5.");
        }
    }
}


