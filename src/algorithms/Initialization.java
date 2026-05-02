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

        boolean addDummyCustomer;
        try {
            addDummyCustomer = !Tools.isBalanced(g);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(
                    "BalasHammer supports only balanced graphs or graphs with supply greater than demand.",
                    e
            );
        }

        int[] rowToProvision = new int[provisions.size()];
        int[] colToCustomer = new int[customers.size() + (addDummyCustomer ? 1 : 0)];
        for (int i = 0; i < provisions.size(); i++) {
            rowToProvision[i] = i;
        }
        for (int j = 0; j < customers.size(); j++) {
            colToCustomer[j] = j;
        }

        if (addDummyCustomer) {
            colToCustomer[colToCustomer.length - 1] = -1;
        }

        int[][] costs = buildCostMatrix(provisions, customers, addDummyCustomer);

        int[] workingSupply = new int[rowToProvision.length];
        int[] workingDemand = new int[colToCustomer.length];
        System.arraycopy(remainingSupply, 0, workingSupply, 0, provisions.size());
        System.arraycopy(remainingDemand, 0, workingDemand, 0, customers.size());
        if (addDummyCustomer) {
            workingDemand[workingDemand.length - 1] = totalSupply - totalDemand;
        }

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
}
