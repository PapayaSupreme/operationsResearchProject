package structure;

import utilities.IdGenerator;

public class Vertex {
    private final int id;
    private final String name;

    public Vertex(String name){
        this.id = IdGenerator.generateId();
        this.name = name;
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
}
