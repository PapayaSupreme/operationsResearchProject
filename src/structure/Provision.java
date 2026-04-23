package structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Provision extends Vertex {
    private final Map<Integer, Customer> costs = new HashMap<>();
    private int provision;

    public Provision(String name, int provision) {
        super(name);
        this.provision = provision;
    }

    public int getProvision() { return this.provision; }
    public Map<Integer, Customer> getCosts() { return Collections.unmodifiableMap(this.costs); }

    public void setProvision(int provision){ this.provision = provision; }

    public void addEdge(int cost, Customer customer){
        this.costs.put(cost, customer);
    }

    /**
     * This method attempts to remove a specified amount of provision.
     * If the current provision is sufficient to cover the removal, it deducts the specified amount and returns true.
     * If the provision is insufficient, it leaves the provision unchanged and returns false.
     *
     * @param provision - the amount of provision to be removed
     * @return boolean - true if the provision was successfully removed, false otherwise
     */
    public boolean removeProvision(int provision) {
        if (this.provision - provision >= 0){
            this.provision -= provision;
        return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Provision" + super.toString();
    }
}
