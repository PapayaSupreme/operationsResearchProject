package structure;

import utilities.IdGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Provision getProvision(int id) { return this.provisions.get(id); }
    public Customer getCustomer(int id) { return this.customers.get(id); }

    public void addProvision(Provision provision){ this.provisions.put(provision.getId(), provision); }
    public void addCustomer(Customer customer){ this.customers.put(customer.getId(), customer); }

    @Override
    public String toString() {
        List<Provision> sortedProvisions = new ArrayList<>(this.provisions.values());
        List<Customer> sortedCustomers = new ArrayList<>(this.customers.values());
        // Use numeric-aware sorting so names like C2 come before C11
        sortedProvisions.sort(Comparator
                .comparingInt((Provision p) -> extractNumberFromName(p.getName()))
                .thenComparing(Provision::getName));
        sortedCustomers.sort(Comparator
                .comparingInt((Customer c) -> extractNumberFromName(c.getName()))
                .thenComparing(Customer::getName));

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
                table.append("|").append(formatCell(formatShipmentAndCost(provision, customer), columnWidth));
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

    private static String formatShipmentAndCost(Provision provision, Customer customer) {
        Integer shipment = provision.getShippings().get(customer);
        String shipmentValue = shipment == null ? "-" : String.valueOf(shipment);
        return shipmentValue + "(" + provision.getCosts().get(customer) + ")";
    }

    private static String formatCell(String value, int width) {
        return String.format("%" + width + "s", value);
    }

    // Extract the first integer found in a name (e.g. "C11" -> 11). If no integer found return Integer.MAX_VALUE
    private static int extractNumberFromName(String name) {
        if (name == null) return Integer.MAX_VALUE;
        Matcher m = Pattern.compile("(\\d+)").matcher(name);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return Integer.MAX_VALUE;
    }
}
