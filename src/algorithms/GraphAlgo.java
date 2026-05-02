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

        if (vertex instanceof Provision p) {
            neighbors.addAll(p.getShippings().keySet());
        } else if (vertex instanceof Customer c) {
            for (Provision p : graph.getProvisions().values()) {
                if (p.getShippings().containsKey(c)) {
                    neighbors.add(p);
                }
            }
        }

        return neighbors;
    }

    private static List<Object> getNeighbors(Graph graph, Object vertex) {
        List<Object> neighbors = new ArrayList<Object>();

        if (vertex instanceof Provision provision) {

            for (Map.Entry<Customer, Integer> entry
                    : provision.getShippings().entrySet()) {

                Integer quantity = entry.getValue();

                if (quantity != null && quantity > 0) {
                    neighbors.add(entry.getKey());
                }
            }
        } else if (vertex instanceof Customer customer) {

            for (Provision provision : graph.getProvisions().values()) {
                Integer quantity =
                        provision.getShippings().get(customer);

                if (quantity != null && quantity > 0) {
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

        Object start = allVertices.getFirst();
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

    public static List<Object> buildCycle(
            Graph graph,
            Object start,
            Object end) {

        // Build cycle as: start -> end -> ... -> start
        // so the entering edge (start -> end) is always the first '+' edge.
        List<Object> path = findPath(graph, end, start);

        if (path.isEmpty()) {
            throw new IllegalStateException(
                    "No path found between vertices.");
        }

        List<Object> cycle = new ArrayList<Object>();
        cycle.add(start);
        cycle.addAll(path);

        if (cycle.size() < 4 || !cycle.getLast().equals(start)) {
            throw new IllegalStateException("Invalid cycle built for entering edge.");
        }

        return cycle;
    }

    public static void maximizeCycle(
            Graph graph,
            List<Object> cycle) {

        if (graph == null || cycle == null || cycle.size() < 4) {
            throw new IllegalArgumentException("Invalid cycle.");
        }

        validateAlternatingCycle(cycle);

        int delta = Integer.MAX_VALUE;

        for (int i = 0; i < cycle.size() - 1; i++) {
            if (i % 2 == 1) {
                int flow = getFlow(
                        cycle.get(i),
                        cycle.get(i + 1));

                if (flow <= 0) {
                    throw new IllegalStateException("Invalid '-' edge in cycle: non-positive flow.");
                }

                delta = Math.min(delta, flow);
            }
        }

        if (delta == Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "No valid '-' edges found in cycle.");
        }

        for (int i = 0; i < cycle.size() - 1; i++) {
            Object from = cycle.get(i);
            Object to = cycle.get(i + 1);

            int currentFlow = getFlow(from, to);

            if (i % 2 == 0) {
                setFlow(from, to, currentFlow + delta);
            } else {
                int newFlow = currentFlow - delta;

                if (newFlow == 0) {
                    removeFlow(from, to);
                } else {
                    setFlow(from, to, newFlow);
                }
            }
        }
    }

    private static void validateAlternatingCycle(List<Object> cycle) {
        for (int i = 0; i < cycle.size() - 1; i++) {
            Object from = cycle.get(i);
            Object to = cycle.get(i + 1);

            boolean validEdge =
                    (from instanceof Provision && to instanceof Customer)
                            || (from instanceof Customer && to instanceof Provision);

            if (!validEdge) {
                throw new IllegalArgumentException("Invalid cycle: edges must alternate Provision/Customer.");
            }
        }
    }

    private static int getFlow(
            Object from,
            Object to) {

        if (from instanceof Provision p && to instanceof Customer c) {
            return p.getShippings().getOrDefault(c, 0);
        }

        if (from instanceof Customer c && to instanceof Provision p) {
            return p.getShippings().getOrDefault(c, 0);
        }

        throw new IllegalArgumentException("Invalid edge.");
    }

    private static void setFlow(
            Object from,
            Object to,
            int value) {

        if (from instanceof Provision p && to instanceof Customer c) {
            p.addShipment(c, value);
            return;
        }

        if (from instanceof Customer c && to instanceof Provision p) {
            p.addShipment(c, value);
            return;
        }

        throw new IllegalArgumentException("Invalid edge.");
    }

    private static void removeFlow(
            Object from,
            Object to) {

        if (from instanceof Provision p && to instanceof Customer c) {
            p.removeShipment(c);
            return;
        }

        if (from instanceof Customer c && to instanceof Provision p) {
            p.removeShipment(c);
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

            if (current instanceof Provision p) {

                for (Map.Entry<Customer, Integer> entry
                        : p.getShippings().entrySet()) {

                    Customer c = entry.getKey();

                    if (!potential.containsKey(c)) {
                        int cost = p.getCosts().get(c);
                        potential.put(
                                c,
                                cost - potential.get(p));
                        queue.add(c);
                    }
                }

            } else if (current instanceof Customer c) {

                for (Provision p
                        : graph.getProvisions().values()) {

                    if (p.getShippings().containsKey(c)
                            && !potential.containsKey(p)) {

                        int cost = p.getCosts().get(c);
                        potential.put(
                                p,
                                cost - potential.get(c));
                        queue.add(p);
                    }
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

                if (shipping != null && shipping > 0) {
                    continue;
                }

                int cost = p.getCosts().get(c);
                int u = potential.getOrDefault(p, 0);
                int v = potential.getOrDefault(c, 0);

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
}
