package algorithms;

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
}
