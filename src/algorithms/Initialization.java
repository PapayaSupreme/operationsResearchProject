package algorithms;

import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static algorithms.Tools.buildCostMatrix;

public class Initialization {
    /**
     * This method implements the North-West corner method for initializing a transportation problem.
     * Complexity: O(P + C)
     *
     * @param g (Graph) - the graph to be worked on
     */
    public static void NorthWest(Graph g) {

        if (g == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        List<Provision> provisions = new ArrayList<>(g.getProvisions().values());
        List<Customer> customers = new ArrayList<>(g.getCustomers().values());

        provisions.sort(Comparator.comparingInt(Provision::getId));
        customers.sort(Comparator.comparingInt(Customer::getId));

        ensureDummyCustomerIfNeeded(g, provisions, customers);

        for (Provision provision : provisions) {
            provision.clearShipments();
        }

        int[] remainingSupply = new int[provisions.size()];
        int[] remainingDemand = new int[customers.size()];

        for (int i = 0; i < provisions.size(); i++) {
            remainingSupply[i] = provisions.get(i).getProvision();
        }
        for (int j = 0; j < customers.size(); j++) {
            remainingDemand[j] = customers.get(j).getOrder();
        }

        int i = 0;
        int j = 0;

        while (i < provisions.size() && j < customers.size()) {
            int quantity = Math.min(remainingSupply[i], remainingDemand[j]);
            if (quantity > 0) {
                provisions.get(i).addShipment(customers.get(j), quantity);
            }

            remainingSupply[i] -= quantity;
            remainingDemand[j] -= quantity;

            if (remainingSupply[i] == 0) {
                i++;
            }
            if (remainingDemand[j] == 0) {
                j++;
            }
        }

        // Ensure basis is connected by adding zero-flow artificial edges if needed
        ensureConnectedBasis(g, provisions, customers);
    }

    /**
     * This method implements the Balas-Hammer first working solution algorithm.
     * Complexity: O(P²C + PC²)
     * @param g - The graph to be worked on.
     */
    public static void BalasHammer(Graph g){

        if (g == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        List<Provision> provisions = new ArrayList<>(g.getProvisions().values());
        List<Customer> customers = new ArrayList<>(g.getCustomers().values());

        provisions.sort(Comparator.comparingInt(Provision::getId));
        customers.sort(Comparator.comparingInt(Customer::getId));

        ensureDummyCustomerIfNeeded(g, provisions, customers);

        for (Provision provision : provisions) {
            provision.clearShipments();
        }

        int[] remainingSupply = new int[provisions.size()];
        int[] remainingDemand = new int[customers.size()];
        int totalSupply = 0;
        int totalDemand = 0;

        for (int i = 0; i < provisions.size(); i++) {
            remainingSupply[i] = provisions.get(i).getProvision();
            totalSupply += remainingSupply[i];
        }
        for (int j = 0; j < customers.size(); j++) {
            remainingDemand[j] = customers.get(j).getOrder();
            totalDemand += remainingDemand[j];
        }

        int[] rowToProvision = new int[provisions.size()];
        int[] colToCustomer = new int[customers.size()];
        for (int i = 0; i < provisions.size(); i++) {
            rowToProvision[i] = i;
        }
        for (int j = 0; j < customers.size(); j++) {
            colToCustomer[j] = j;
        }

        int[][] costs = buildCostMatrix(provisions, customers, false);

        int[] workingSupply = new int[rowToProvision.length];
        int[] workingDemand = new int[colToCustomer.length];
        System.arraycopy(remainingSupply, 0, workingSupply, 0, provisions.size());
        System.arraycopy(remainingDemand, 0, workingDemand, 0, customers.size());

        boolean[] activeRows = new boolean[rowToProvision.length];
        boolean[] activeCols = new boolean[colToCustomer.length];
        for (int i = 0; i < activeRows.length; i++) {
            activeRows[i] = workingSupply[i] > 0;
        }
        for (int j = 0; j < activeCols.length; j++) {
            activeCols[j] = workingDemand[j] > 0;
        }

        while (hasActive(activeRows) && hasActive(activeCols)) {
            Selection bestSelection = null;

            for (int i = 0; i < activeRows.length; i++) {
                if (!activeRows[i]) {
                    continue;
                }
                Selection rowSelection = evaluateRow(i, costs, workingSupply, workingDemand, activeCols);
                bestSelection = selectBetter(bestSelection, rowSelection);
            }

            for (int j = 0; j < activeCols.length; j++) {
                if (!activeCols[j]) {
                    continue;
                }
                Selection columnSelection = evaluateColumn(j, costs, workingSupply, workingDemand, activeRows);
                bestSelection = selectBetter(bestSelection, columnSelection);
            }

            if (bestSelection == null) {
                break;
            }

            int quantity = Math.min(workingSupply[bestSelection.rowIndex], workingDemand[bestSelection.columnIndex]);
            int provisionIndex = rowToProvision[bestSelection.rowIndex];
            int customerIndex = colToCustomer[bestSelection.columnIndex];
            if (quantity > 0 && provisionIndex >= 0 && customerIndex >= 0) {
                provisions.get(provisionIndex).addShipment(customers.get(customerIndex), quantity);
            }

            workingSupply[bestSelection.rowIndex] -= quantity;
            workingDemand[bestSelection.columnIndex] -= quantity;

            if (workingSupply[bestSelection.rowIndex] == 0) {
                activeRows[bestSelection.rowIndex] = false;
            }
            if (workingDemand[bestSelection.columnIndex] == 0) {
                activeCols[bestSelection.columnIndex] = false;
            }
        }

        // Ensure basis is connected by adding zero-flow artificial edges if needed
        ensureConnectedBasis(g, provisions, customers);
    }

    private static Selection evaluateRow(int rowIndex, int[][] costs, int[] remainingSupply, int[] remainingDemand, boolean[] activeCols) {
        int smallest = Integer.MAX_VALUE;
        int secondSmallest = Integer.MAX_VALUE;
        int bestColumn = -1;

        for (int columnIndex = 0; columnIndex < activeCols.length; columnIndex++) {
            if (!activeCols[columnIndex]) {
                continue;
            }

            int cost = costs[rowIndex][columnIndex];
            if (cost < smallest) {
                secondSmallest = smallest;
                smallest = cost;
                bestColumn = columnIndex;
            } else if (cost < secondSmallest) {
                secondSmallest = cost;
            }
        }

        if (bestColumn == -1) {
            return null;
        }

        int penalty = secondSmallest == Integer.MAX_VALUE ? Integer.MAX_VALUE / 4 : secondSmallest - smallest;
        int allocation = Math.min(remainingSupply[rowIndex], remainingDemand[bestColumn]);
        return new Selection(true, rowIndex, bestColumn, penalty, smallest, allocation);
    }

    private static Selection evaluateColumn(int columnIndex, int[][] costs, int[] remainingSupply, int[] remainingDemand, boolean[] activeRows) {
        int smallest = Integer.MAX_VALUE;
        int secondSmallest = Integer.MAX_VALUE;
        int bestRow = -1;

        for (int rowIndex = 0; rowIndex < activeRows.length; rowIndex++) {
            if (!activeRows[rowIndex]) {
                continue;
            }

            int cost = costs[rowIndex][columnIndex];
            if (cost < smallest) {
                secondSmallest = smallest;
                smallest = cost;
                bestRow = rowIndex;
            } else if (cost < secondSmallest) {
                secondSmallest = cost;
            }
        }

        if (bestRow == -1) {
            return null;
        }

        int penalty = secondSmallest == Integer.MAX_VALUE ? Integer.MAX_VALUE / 4 : secondSmallest - smallest;
        int allocation = Math.min(remainingSupply[bestRow], remainingDemand[columnIndex]);
        return new Selection(false, bestRow, columnIndex, penalty, smallest, allocation);
    }

    private static Selection selectBetter(Selection currentBest, Selection candidate) {
        if (candidate == null) {
            return currentBest;
        }
        if (currentBest == null) {
            return candidate;
        }

        if (candidate.penalty != currentBest.penalty) {
            return candidate.penalty > currentBest.penalty ? candidate : currentBest;
        }
        if (candidate.cheapestCost != currentBest.cheapestCost) {
            return candidate.cheapestCost < currentBest.cheapestCost ? candidate : currentBest;
        }
        if (candidate.allocation != currentBest.allocation) {
            return candidate.allocation > currentBest.allocation ? candidate : currentBest;
        }
        if (candidate.isRowChoice != currentBest.isRowChoice) {
            return candidate.isRowChoice ? candidate : currentBest;
        }
        if (candidate.rowIndex != currentBest.rowIndex) {
            return candidate.rowIndex < currentBest.rowIndex ? candidate : currentBest;
        }
        if (candidate.columnIndex != currentBest.columnIndex) {
            return candidate.columnIndex < currentBest.columnIndex ? candidate : currentBest;
        }
        return currentBest;
    }

    private static boolean hasActive(boolean[] active) {
        for (boolean value : active) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    private record Selection(boolean isRowChoice,
                             int rowIndex,
                             int columnIndex,
                             int penalty,
                             int cheapestCost,
                             int allocation) {}

    private static void ensureDummyCustomerIfNeeded(
            Graph g,
            List<Provision> provisions,
            List<Customer> customers) {

        boolean balanced;
        try {
            balanced = Tools.isBalanced(g);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(
                    "Initialization supports only balanced graphs or graphs with supply greater than demand.",
                    e
            );
        }

        if (balanced) {
            return;
        }

        int totalSupply = totalProvision(provisions);
        int totalDemand = totalDemand(customers);

        if (totalSupply < totalDemand) {
            throw new UnsupportedOperationException(
                    "Initialization supports only balanced graphs or graphs with supply greater than demand."
            );
        }

        Customer dummy = new Customer("Dummy", totalSupply - totalDemand);
        g.addCustomer(dummy);

        for (Provision provision : provisions) {
            provision.addEdge(dummy, 0);
        }

        customers.add(dummy);
        customers.sort(Comparator.comparingInt(Customer::getId));
    }

    private static int totalProvision(List<Provision> provisions) {
        int total = 0;
        for (Provision provision : provisions) {
            total += provision.getProvision();
        }
        return total;
    }

    private static int totalDemand(List<Customer> customers) {
        int total = 0;
        for (Customer customer : customers) {
            total += customer.getOrder();
        }
        return total;
    }

    /**
     * Ensure the basis is connected by adding zero-flow artificial edges.
     * This is necessary for stepping-stone to work on degenerate/disconnected bases.
     * Strategy: Add edges between connected components to form a spanning tree.
     */
    private static void ensureConnectedBasis(Graph g, List<Provision> provisions, List<Customer> customers) {
        // Check if basis is already connected
        if (GraphAlgo.isConnectedIncludingZeroFlow(g)) {
            return;
        }

        // Keep adding edges until connected
        while (!GraphAlgo.isConnectedIncludingZeroFlow(g)) {
            // Find two disconnected components
            java.util.Set<Object> component1 = new java.util.HashSet<>();
            java.util.Set<Object> visited = new java.util.HashSet<>();

            // Start BFS from first provision
            Provision startP = provisions.get(0);
            java.util.Queue<Object> queue = new java.util.LinkedList<>();
            queue.add(startP);
            visited.add(startP);
            component1.add(startP);

            while (!queue.isEmpty()) {
                Object current = queue.poll();

                if (current instanceof Provision prov) {
                    for (Customer c : prov.getShippings().keySet()) {
                        if (!visited.contains(c)) {
                            visited.add(c);
                            component1.add(c);
                            queue.add(c);
                        }
                    }
                } else if (current instanceof Customer cust) {
                    for (Provision prov : provisions) {
                        if (prov.getShippings().containsKey(cust) && !visited.contains(prov)) {
                            visited.add(prov);
                            component1.add(prov);
                            queue.add(prov);
                        }
                    }
                }
            }

            // Find an unvisited vertex
            Provision unvisitedP = null;
            Customer unvisitedC = null;

            for (Provision p : provisions) {
                if (!visited.contains(p)) {
                    unvisitedP = p;
                    break;
                }
            }

            for (Customer c : customers) {
                if (!visited.contains(c)) {
                    unvisitedC = c;
                    break;
                }
            }

            // Connect them
            if (unvisitedP != null) {
                // Find a customer in component1 to connect to
                Customer connectedC = null;
                for (Object obj : component1) {
                    if (obj instanceof Customer) {
                        connectedC = (Customer) obj;
                        break;
                    }
                }
                if (connectedC != null && unvisitedP.getShippings().get(connectedC) == null) {
                    unvisitedP.addShipment(connectedC, 0);
                }
            } else if (unvisitedC != null) {
                // Find a provision in component1 to connect to
                Provision connectedP = null;
                for (Object obj : component1) {
                    if (obj instanceof Provision) {
                        connectedP = (Provision) obj;
                        break;
                    }
                }
                if (connectedP != null && connectedP.getShippings().get(unvisitedC) == null) {
                    connectedP.addShipment(unvisitedC, 0);
                }
            }
        }
    }
}
