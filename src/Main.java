import algorithms.NorthWest;
import structure.Graph;

void main() {
    Graph g1 = MatrixReader.read("src/problems/problem1.txt");
    System.out.println(g1);
    NorthWest.computeNorthWest(g1);
    System.out.println(g1);
}
