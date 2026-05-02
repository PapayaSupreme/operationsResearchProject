package algorithms;

import java.util.List;
import java.util.Optional;
import structure.Customer;
import structure.Graph;
import structure.Provision;

public class Tools {
    /**
     * This method implements a balance check for a given Graph g. Will return either true or false,
     * or throw an exception depending on the relationship between the total provision and total orders in the graph.
     *
     * @param g (Graph) - The graph to be worked on
     * @return boolean - true if the total provision equals the total orders, false if the total provision exceeds the total orders
     * @throws IllegalArgumentException if the total orders exceed the total provision.
     */
    public static boolean isBalanced(Graph g){

        if (g == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        int totalProvision = 0;
        int totalOrders = 0;

        for (Provision p : g.getProvisions().values()){
            totalProvision += p.getProvision();
        }
        for (Customer c : g.getCustomers().values()){
            totalOrders += c.getOrder();
        }

        if (totalProvision == totalOrders){
            return true;
        } else if (totalProvision > totalOrders){
            return false;
        } else {
            throw new IllegalArgumentException("Graph cannot have more orders than provisions.");
        }
    }

    public static Optional<Integer> totalCost(Graph g){
        if (g == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }
        boolean isNotEmpty = false;
        int totalCost = 0;
        for (Provision p : g.getProvisions().values()){
            for (Customer shippedTo : p.getShippings().keySet()){
                isNotEmpty = true;
                totalCost += p.getCosts().get(shippedTo) * p.getShippings().get(shippedTo);
            }
        }

        return isNotEmpty ? Optional.of(totalCost) :  Optional.empty();
    }

    public static int[][] buildCostMatrix(List<Provision> provisions, List<Customer> customers, boolean addDummyCustomer) {
        int[][] costs = new int[provisions.size()][customers.size() + (addDummyCustomer ? 1 : 0)];

        for (int i = 0; i < provisions.size(); i++) {
            Provision provision = provisions.get(i);
            for (int j = 0; j < customers.size(); j++) {
                Customer customer = customers.get(j);
                Integer cost = provision.getCosts().get(customer);
                if (cost == null) {
                    throw new IllegalStateException(
                            "Missing cost for provision " + provision.getName() + " and customer " + customer.getName() + "."
                    );
                }
                costs[i][j] = cost;
            }
        }

        if (addDummyCustomer) {
            int dummyColumnIndex = costs[0].length - 1;
            for (int i = 0; i < costs.length; i++) {
                costs[i][dummyColumnIndex] = 0;
            }
        }

        return costs;
    }
}
