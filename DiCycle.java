import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.Stack;

public class DiCycle {

    private final Digraph graph;
    private boolean[] marked;
    private boolean[] onStack;
    private Integer[] edgeTo;
    private Stack<Integer> cycle;

    public DiCycle(Digraph G) {
        graph = G;
        marked = new boolean[G.V()];
        onStack = new boolean[G.V()];
        edgeTo = new Integer[G.V()];
        for (int i = 0; i < G.V(); i++) {
            if (!marked[i]) {
                edgeTo[i] = i;
                cycleDFS(i);
            }
        }
    }

    private void cycleDFS(int v) {
        marked[v] = true;
        onStack[v] = true;
        for (int w : graph.adj(v)) {

            if (cycle != null) return;

            if (!marked[w]) {
                edgeTo[w] = v;
                cycleDFS(w);
            }
            // assume self loop is a cycle
            else if (onStack[w]) {
                cycle = new Stack<>();
                for (int prev = v; prev != w; prev = edgeTo[prev])
                    cycle.push(w);

            cycle.push(w);
            // cycle representation will start and finish with the same vertex i.e. has duplicate
            cycle.push(v);
            }
        }
        onStack[v] = false;
    }

    public boolean hasCycle() {
        return cycle != null;
    }

    public Iterable<Integer> getCycle() {
        return cycle;
    }
}
