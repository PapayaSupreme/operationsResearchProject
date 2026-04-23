package structure;

import utilities.IdGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final String name;
    private final int id;
    private final Map<Integer, Provision> provisions = new HashMap<>();
    private final Map<Integer, Customer> customers = new HashMap<>();

    public Graph(String name){
        this.name = name;
        this.id = IdGenerator.generateId();
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }

    public Map<Integer, Provision> getProvisions() { return Collections.unmodifiableMap(this.provisions); }
    public Map<Integer, Customer> getCustomers() { return Collections.unmodifiableMap(this.customers); }

    public void addProvision(Provision provision){ this.provisions.put(provision.getId(), provision); }
    public void addCustomer(Customer customer){ this.customers.put(customer.getId(), customer); }

    @Override
    public String toString() {
        List<Provision> sortedProvisions = new ArrayList<>(this.provisions.values());
        List<Customer> sortedCustomers = new ArrayList<>(this.customers.values());
        sortedProvisions.sort(Comparator.comparing(Provision::getName));
        sortedCustomers.sort(Comparator.comparing(Customer::getName));

        int columnWidth = 11; //static width, if we have huge provisions then it overflows careful
        StringBuilder table = new StringBuilder();
        table.append("Graph: ID = ").append(this.id).append(", name = ").append(this.name).append("\n");

        table.append(formatCell("P\\C", columnWidth));
        for (Customer customer : sortedCustomers) {
            table.append("|").append(formatCell(customer.getName(), columnWidth));
        }
        table.append("|").append(formatCell("Provision", columnWidth)).append("\n");

        int totalColumns = sortedCustomers.size() + 2;
        table.repeat("-", columnWidth * totalColumns + sortedCustomers.size() + 1).append("\n");

        for (Provision provision : sortedProvisions) {
            table.append(formatCell(provision.getName(), columnWidth));
            for (Customer customer : sortedCustomers) {
                table.append("|").append(formatCell(findCost(provision, customer), columnWidth));
            }
            table.append("|").append(formatCell(String.valueOf(provision.getProvision()), columnWidth)).append("\n");
        }

        table.repeat("-", columnWidth * totalColumns + sortedCustomers.size() + 1).append("\n");
        table.append(formatCell("Order", columnWidth));
        for (Customer customer : sortedCustomers) {
            table.append("|").append(formatCell(String.valueOf(customer.getOrder()), columnWidth));
        }
        table.append("|").append(formatCell("", columnWidth));

        return table.toString();
    }

    private static String findCost(Provision provision, Customer customer) {
        for (Map.Entry<Integer, Customer> entry : provision.getCosts().entrySet()) {
            if (entry.getValue().getId() == customer.getId()) {
                return String.valueOf(entry.getKey());
            }
        }
        return "-";
    }

    private static String formatCell(String value, int width) {
        return String.format("%" + width + "s", value);
    }
}
