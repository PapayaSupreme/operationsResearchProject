package algorithms;

import structure.Graph;
import structure.Customer;
import structure.Provision;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NorthWest {
    public static void computeNorthWest(Graph g){
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
}
