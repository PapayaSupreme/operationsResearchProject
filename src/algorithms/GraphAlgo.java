package algorithms;

import structure.Customer;
import structure.Graph;
import structure.Provision;

import java.util.*;

/**
 * Graph algorithms used in transportation problems.
 */
public final class GraphAlgo {

    private GraphAlgo() {
    }

    public static boolean isAcyclic(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        Set<Object> visited = new HashSet<Object>();
        Map<Object, Object> parent = new HashMap<Object, Object>();

        List<Object> allVertices = new ArrayList<Object>();
        allVertices.addAll(graph.getProvisions().values());
        allVertices.addAll(graph.getCustomers().values());

        for (Object vertex : allVertices) {
            if (visited.contains(vertex)) {
                continue;
            }

            parent.clear();

            if (!bfsDetectCycle(graph, vertex, visited, parent)) {
                return false;
            }
        }

        return true;
    }

    private static boolean bfsDetectCycle(
            Graph graph,
            Object start,
            Set<Object> visited,
            Map<Object, Object> parent) {

        Queue<Object> queue = new LinkedList<Object>();

        visited.add(start);
        parent.put(start, null);
        queue.add(start);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            for (Object neighbor : getNeighbors(graph, current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                } else if (!neighbor.equals(parent.get(current))) {
                    return false;
                }
            }
        }

        return true;
    }

    private static List<Object> getNeighbors(Graph graph, Object vertex) {
        List<Object> neighbors = new ArrayList<Object>();

        if (vertex instanceof Provision) {
            Provision provision = (Provision) vertex;

            for (Map.Entry<Customer, Integer> entry
                    : provision.getShippings().entrySet()) {

                Integer quantity = entry.getValue();

                if (quantity != null && quantity.intValue() > 0) {
                    neighbors.add(entry.getKey());
                }
            }
        } else if (vertex instanceof Customer) {
            Customer customer = (Customer) vertex;

            for (Provision provision : graph.getProvisions().values()) {
                Integer quantity =
                        provision.getShippings().get(customer);

                if (quantity != null && quantity.intValue() > 0) {
                    neighbors.add(provision);
                }
            }
        }

        return neighbors;
    }

    public static boolean isConnected(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null.");
        }

        List<Object> allVertices = new ArrayList<Object>();
        allVertices.addAll(graph.getProvisions().values());
        allVertices.addAll(graph.getCustomers().values());

        if (allVertices.isEmpty()) {
            return true;
        }

        Set<Object> visited = new HashSet<Object>();
        Queue<Object> queue = new LinkedList<Object>();

        Object start = allVertices.get(0);
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            for (Object neighbor : getNeighbors(graph, current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() == allVertices.size();
    }

    public static List<Object> findPath(
            Graph graph,
            Object start,
            Object end) {

        Map<Object, Object> parent = new HashMap<Object, Object>();
        Set<Object> visited = new HashSet<Object>();
        Queue<Object> queue = new LinkedList<Object>();

        visited.add(start);
        parent.put(start, null);
        queue.add(start);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            if (current.equals(end)) {
                break;
            }

            for (Object neighbor : getNeighbors(graph, current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<Object> path = new ArrayList<Object>();

        if (!parent.containsKey(end)) {
            return path;
        }

        Object step = end;
        while (step != null) {
            path.add(step);
            step = parent.get(step);
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Find a path between two vertices, INCLUDING zero-flow edges.
     * This is used by buildCycle() to find cycles in graphs with zero-flow edges.
     */
    public static List<Object> findPathIncludingZeroFlow(
            Graph graph,
            Object start,
            Object end) {

        Map<Object, Object> parent = new HashMap<Object, Object>();
        Set<Object> visited = new HashSet<Object>();
        Queue<Object> queue = new LinkedList<Object>();

        visited.add(start);
        parent.put(start, null);
        queue.add(start);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            if (current.equals(end)) {
                break;
            }

            // CRITICAL: Use getNeighborsIncludingZeroFlow instead of getNeighbors
            for (Object neighbor : getNeighborsIncludingZeroFlow(graph, current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<Object> path = new ArrayList<Object>();

        if (!parent.containsKey(end)) {
            return path;
        }

        Object step = end;
        while (step != null) {
            path.add(step);
            step = parent.get(step);
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Build a cycle for the stepping-stone method.
     * 
     * The entering edge is: start (Provision) → end (Customer)
     * The cycle must close: [start, end, ..., start]
     */
    public static List<Object> buildCycle(
        Graph graph,
        Object start,  // Provision where entering edge starts  
        Object end) {  // Customer where entering edge ends
    
    // Find path from end back to start (including zero-flow edges)
    List<Object> pathBackToStart = findPathIncludingZeroFlow(graph, end, start);
    
    if (pathBackToStart.isEmpty()) {
        throw new IllegalStateException(
                "No path found between vertices.");
    }
    
    // pathBackToStart is: [end, intermediate vertices..., start]
    // We want cycle: [start, end, intermediate vertices..., start]
    
    List<Object> cycle = new ArrayList<>();
    cycle.add(start);  // Add start first
    
    // Add all vertices from pathBackToStart (which includes end and ends with start)
    cycle.addAll(pathBackToStart);
    
    // Now cycle is: [start, end, ..., start] - properly closed!
    return cycle;
}

    /**
     * Maximize flow along a cycle using the stepping-stone method.
     * 
     * The cycle alternates between provisions and customers.
     * Edges at EVEN indices (0, 2, 4, ...) get INCREASED by delta (+)
     * Edges at ODD indices (1, 3, 5, ...) get DECREASED by delta (-)
     * 
     * We find delta as the minimum flow on the "-" edges.
     */
    public static void maximizeCycle(
            Graph graph,
            List<Object> cycle) {

        if (graph == null || cycle == null || cycle.size() < 3) {
            throw new IllegalArgumentException("Invalid cycle.");
        }

        // Step 1: Find delta (minimum flow on "-" edges)
        int delta = Integer.MAX_VALUE;

        // Look at edges between consecutive vertices in the cycle
        for (int i = 0; i < cycle.size() - 1; i++) {
            Object from = cycle.get(i);
            Object to = cycle.get(i + 1);
            
            // Odd indices are "-" edges (they will have flow DECREASED)
            if (i % 2 == 1) {
                int flow = getFlow(graph, from, to);
                delta = Math.min(delta, flow);
            }
        }

        if (delta == Integer.MAX_VALUE || delta == 0) {
            throw new IllegalStateException(
                    "No valid '-' edges found in cycle.");
        }

        // Step 2: Adjust flows along the cycle
        for (int i = 0; i < cycle.size() - 1; i++) {
            Object from = cycle.get(i);
            Object to = cycle.get(i + 1);

            int currentFlow = getFlow(graph, from, to);

            // Even indices: add delta ("+")
            if (i % 2 == 0) {
                setFlow(graph, from, to, currentFlow + delta);
            } 
            // Odd indices: subtract delta ("-")
            else {
                int newFlow = currentFlow - delta;

                if (newFlow == 0) {
                    removeFlow(graph, from, to);
                } else {
                    setFlow(graph, from, to, newFlow);
                }
            }
        }
    }

    private static int getFlow(
            Graph graph,
            Object from,
            Object to) {

        if (from instanceof Provision && to instanceof Customer) {
            Provision p = (Provision) from;
            Customer c = (Customer) to;
            return p.getShippings().getOrDefault(c, 0);
        }

        if (from instanceof Customer && to instanceof Provision) {
            Customer c = (Customer) from;
            Provision p = (Provision) to;
            return p.getShippings().getOrDefault(c, 0);
        }

        throw new IllegalArgumentException("Invalid edge.");
    }

    private static void setFlow(
            Graph graph,
            Object from,
            Object to,
            int value) {

        if (from instanceof Provision && to instanceof Customer) {
            Provision p = (Provision) from;
            Customer c = (Customer) to;
            p.addShipment(c, value);
            return;
        }

        if (from instanceof Customer && to instanceof Provision) {
            Customer c = (Customer) from;
            Provision p = (Provision) to;
            p.addShipment(c, value);
            return;
        }

        throw new IllegalArgumentException("Invalid edge.");
    }

    private static void removeFlow(
            Graph graph,
            Object from,
            Object to) {

        if (from instanceof Provision && to instanceof Customer) {
            Provision p = (Provision) from;
            Customer c = (Customer) to;
            p.getShippings().remove(c);
            return;
        }

        if (from instanceof Customer && to instanceof Provision) {
            Customer c = (Customer) from;
            Provision p = (Provision) to;
            p.getShippings().remove(c);
            return;
        }

        throw new IllegalArgumentException("Invalid edge.");
    }

    public static Map<Object, Integer> computePotentials(Graph graph) {
        Map<Object, Integer> potential =
                new HashMap<Object, Integer>();
        Queue<Object> queue = new LinkedList<Object>();

        Provision start =
                graph.getProvisions().values().iterator().next();

        potential.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            if (current instanceof Provision) {
                Provision p = (Provision) current;
                int u = potential.get(p);

                for (Map.Entry<Customer, Integer> entry
                        : p.getShippings().entrySet()) {

                    Integer quantity = entry.getValue();

                    if (quantity == null || quantity.intValue() == 0) {
                        continue;
                    }

                    Customer c = entry.getKey();

                    if (potential.containsKey(c)) {
                        continue;
                    }

                    int cost = p.getCosts().get(c);
                    int v = cost - u;

                    potential.put(c, v);
                    queue.add(c);
                }
            } else if (current instanceof Customer) {
                Customer c = (Customer) current;
                int v = potential.get(c);

                for (Provision p : graph.getProvisions().values()) {
                    Integer quantity = p.getShippings().get(c);

                    if (quantity == null || quantity.intValue() == 0) {
                        continue;
                    }

                    if (potential.containsKey(p)) {
                        continue;
                    }

                    int cost = p.getCosts().get(c);
                    int u = cost - v;

                    potential.put(p, u);
                    queue.add(p);
                }
            }
        }

        return potential;
    }

    public static Map.Entry<Provision, Customer>
    findEnteringEdge(Graph graph) {

        Map<Object, Integer> potential =
                computePotentials(graph);

        int bestDelta = 0;
        Provision bestP = null;
        Customer bestC = null;

        for (Provision p : graph.getProvisions().values()) {
            for (Customer c : graph.getCustomers().values()) {

                Integer shipping = p.getShippings().get(c);

                if (shipping != null && shipping.intValue() > 0) {
                    continue;
                }

                int cost = p.getCosts().get(c);
                int u = potential.containsKey(p)
                        ? potential.get(p) : 0;
                int v = potential.containsKey(c)
                        ? potential.get(c) : 0;

                int delta = cost - (u + v);

                if (delta < bestDelta) {
                    bestDelta = delta;
                    bestP = p;
                    bestC = c;
                }
            }
        }

        if (bestP == null) {
            return null;
        }

        return new AbstractMap.SimpleEntry<Provision, Customer>(
                bestP,
                bestC);
    }

    public static boolean isOptimal(Graph graph) {
        return findEnteringEdge(graph) == null;
    }

private static List<Set<Object>> findConnectedComponents(Graph graph) {
    List<Set<Object>> components = new ArrayList<>();
    Set<Object> visited = new HashSet<>();
    
    List<Object> allVertices = new ArrayList<>();
    allVertices.addAll(graph.getProvisions().values());
    allVertices.addAll(graph.getCustomers().values());
    
    for (Object vertex : allVertices) {
        if (!visited.contains(vertex)) {
            Set<Object> component = new HashSet<>();
            Queue<Object> queue = new LinkedList<>();
            
            queue.add(vertex);
            visited.add(vertex);
            component.add(vertex);
            
            while (!queue.isEmpty()) {
                Object current = queue.poll();
                
                for (Object neighbor : getNeighbors(graph, current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        component.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            
            components.add(component);
        }
    }
    
    return components;
}

/**
 * Ensures the graph is connected by adding zero-flow edges between components.
 */
public static void ensureConnected(Graph graph) {
    if (graph == null) {
        return;
    }
    
    int maxAttempts = 100;
    int attempts = 0;
    
    while (!isConnectedIncludingZeroFlow(graph) && attempts < maxAttempts) {
        attempts++;
        
        List<Set<Object>> components = findConnectedComponentsIncludingZeroFlow(graph);
        
        if (components.size() <= 1) {
            break;
        }
        
        int bestCost = Integer.MAX_VALUE;
        Provision bestP = null;
        Customer bestC = null;
        
        for (int i = 0; i < components.size(); i++) {
            for (int j = i + 1; j < components.size(); j++) {
                Set<Object> comp1 = components.get(i);
                Set<Object> comp2 = components.get(j);
                
                for (Object v1 : comp1) {
                    for (Object v2 : comp2) {
                        Provision p = null;
                        Customer c = null;
                        
                        if (v1 instanceof Provision && v2 instanceof Customer) {
                            p = (Provision) v1;
                            c = (Customer) v2;
                        } else if (v1 instanceof Customer && v2 instanceof Provision) {
                            p = (Provision) v2;
                            c = (Customer) v1;
                        }
                        
                        if (p != null && c != null) {
                            if (!p.getShippings().containsKey(c)) {
                                int cost = p.getCosts().get(c);
                                
                                if (cost < bestCost) {
                                    bestCost = cost;
                                    bestP = p;
                                    bestC = c;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (bestP != null && bestC != null) {
            bestP.addShipment(bestC, 0);
        } else {
            break;
        }
    }
}

/**
 * Check if graph is connected, including zero-flow edges.
 * PUBLIC so TraceGenerator can use it.
 */
public static boolean isConnectedIncludingZeroFlow(Graph graph) {
    if (graph == null) {
        return false;
    }
    
    List<Set<Object>> components = findConnectedComponentsIncludingZeroFlow(graph);
    return components.size() <= 1;
}

/**
 * Find all connected components, including zero-flow edges.
 */
private static List<Set<Object>> findConnectedComponentsIncludingZeroFlow(Graph graph) {
    List<Set<Object>> components = new ArrayList<>();
    Set<Object> visited = new HashSet<>();
    
    List<Object> allVertices = new ArrayList<>();
    allVertices.addAll(graph.getProvisions().values());
    allVertices.addAll(graph.getCustomers().values());
    
    for (Object vertex : allVertices) {
        if (!visited.contains(vertex)) {
            Set<Object> component = new HashSet<>();
            Queue<Object> queue = new LinkedList<>();
            
            queue.add(vertex);
            visited.add(vertex);
            component.add(vertex);
            
            while (!queue.isEmpty()) {
                Object current = queue.poll();
                
                for (Object neighbor : getNeighborsIncludingZeroFlow(graph, current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        component.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            
            components.add(component);
        }
    }
    
    return components;
}

/**
 * Get all neighbors of a vertex, INCLUDING zero-flow edges.
 */
private static List<Object> getNeighborsIncludingZeroFlow(Graph graph, Object vertex) {
    List<Object> neighbors = new ArrayList<>();
    
    if (vertex instanceof Provision) {
        Provision p = (Provision) vertex;
        for (Customer c : p.getShippings().keySet()) {
            neighbors.add(c);
        }
    } else if (vertex instanceof Customer) {
        Customer c = (Customer) vertex;
        for (Provision p : graph.getProvisions().values()) {
            if (p.getShippings().containsKey(c)) {
                neighbors.add(p);
            }
        }
    }
    
    return neighbors;
}

}