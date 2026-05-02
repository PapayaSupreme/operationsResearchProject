package structure;

public class Customer extends Vertex {
    private int order;

    public Customer(String name, int order){
        super(name);
        this.order = order;
    }

    public int getOrder() { return this.order; }

    public void setOrder(int order){ this.order = order; }

    /**
     * This method attempts to remove a specified amount of order.
     * If the current order is sufficient to accept the provision, it deducts the specified amount and returns true.
     * If the order is insufficient, it leaves the order unchanged and returns false.
     *
     * @param order - the amount of provision to be removed
     * @return boolean - true if the provision was successfully removed, false otherwise
     */
    public boolean removeOrder(int order) {
        if (this.order - order >= 0){
            this.order -= order;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Customer" + super.toString();
    }
}
