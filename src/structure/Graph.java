package structure;

import utilities.IdGenerator;

import java.util.Collections;
import java.util.HashMap;
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
        return "Graph : id = " + this.id + ", name = " + this.name;
    }
}
