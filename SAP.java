
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SAP {

    private final Set<Integer> ancestorsV = new TreeSet<>();
    private final Set<Integer> ancestorsW = new TreeSet<>();

    private final Digraph digraph;
    private final Queue<Integer> queue = new Queue<>();
    private final boolean[] marked;

    private final Integer[] pathLength;
    private final Integer[] pathV;
    private final Integer[] pathW;

    private SoftReference<TreeMap<Integer, Integer[]>> vertCache;
    private SoftReference<HashMap<Iterable<Integer>, Integer[]>> iterableCache;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        if (G == null) throw new IllegalArgumentException();
        digraph = new Digraph(G);
        marked = new boolean[digraph.V()];
        pathLength = new Integer[digraph.V()];
        pathV = new Integer[digraph.V()];
        pathW = new Integer[digraph.V()];
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (v < 0 || v > digraph.V() || w < 0 || w > digraph.V())
            throw new IllegalArgumentException();
        return getAncestorData(v, w)[1];
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (v < 0 || v > digraph.V() || w < 0 || w > digraph.V())
            throw new IllegalArgumentException();
        return getAncestorData(v, w)[0];
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
        for (Integer i : v)
            if (i == null) throw new IllegalArgumentException();
        for (Integer i : w)
            if (i == null) throw new IllegalArgumentException();
        return getAncestorData(v, w)[1];

    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
        for (Integer i : v)
            if (i == null) throw new IllegalArgumentException();
        for (Integer i : w)
            if (i == null) throw new IllegalArgumentException();
        return getAncestorData(v, w)[0];
    }


    private void pathBFS(int v) {
        resetFields();
        // a vertex Is considered an ancestor of itself
        pathLength[v] = 0;
        marked[v] = true;
        queue.enqueue(v);

        // pathLength[i-1]+1
        for (int i = 1; !queue.isEmpty(); i++) {
            for (int n : digraph.adj(queue.dequeue())) {
                if (!marked[n]) {
                    marked[n] = true;
                    queue.enqueue(n);
                    pathLength[n] = i;
                    // TODO: add ancestors thecking
                }
            }
        }
    }


    private Integer[] pathBFS(Iterable<Integer> verts, boolean isA) {
        resetFields();

        Set<Integer> ancestorStorage;

        if (isA) {
            ancestorsV.clear();
            ancestorStorage = ancestorsV;
        }
        else {
            ancestorsW.clear();
            ancestorStorage = ancestorsW;
        }


        for (int v : verts) {
            queue.enqueue(v);
            marked[v] = true;
            pathLength[v] = 0;
            ancestorStorage.add(v);

            for (int i = 1; !queue.isEmpty(); i++) {
                for (int n : digraph.adj(queue.dequeue())) {

                    if (!marked[n]) {
                        marked[n] = true;
                        queue.enqueue(n);
                        pathLength[n] = i;
                        ancestorStorage.add(n);
                    }
                    // if path of marked vertex is less
                    else if (pathLength[n] > i) {
                        queue.enqueue(n);
                        pathLength[n] = i;
                    }
                }
            }
        }
        return pathLength;
    }


    private int[] ancestorLength(Integer[] pathV, Integer[] pathW) {
        // closest common ancestor path
        int ccap = Integer.MAX_VALUE;
        int cca = -1;

        ancestorsV.retainAll(ancestorsW);

        for (Integer i : ancestorsV) {
                if (ccap > pathV[i] + pathW[i]) {
                    ccap = pathV[i] + pathW[i];
                    cca = i;
                }
        }

        if (ccap == Integer.MAX_VALUE) ccap = -1;
        return new int[] { cca, ccap };
    }

    private void resetFields() {
        for (int i = 0; i < digraph.V(); i++) {
            marked[i] = false;
            pathLength[i] = null;
        }
    }


    // don't know how to get rid of these duplicates
    // TODO: this code is buggy and should be editted as its iterable form
    private int[] getAncestorData(int v, int w) {
        if (vertCache != null && vertCache.get() != null) {
            Integer[] cacheV = vertCache.get().get(v);
            if (cacheV == null) {
                pathBFS(v);
                System.arraycopy(pathLength, 0, pathV, 0, pathLength.length);
                vertCache.get().put(v, pathV);
            }
            else {
                //pathV = Arrays.copyOf(cacheV, cacheV.length);
                System.arraycopy(cacheV, 0, pathV, 0, cacheV.length);
            }

            Integer[] cacheW = vertCache.get().get(w);
            if (cacheW == null) {
                pathBFS(w);
                System.arraycopy(pathLength, 0, pathW, 0, pathLength.length);
                vertCache.get().put(w, pathW);
            }
            else {
                System.arraycopy(cacheW, 0, pathW, 0, pathLength.length);
            }
        }
        else {
            vertCache = new SoftReference<TreeMap<Integer, Integer[]>>(
                    new TreeMap<Integer, Integer[]>());
            pathBFS(v);
            System.arraycopy(pathLength, 0, pathV, 0, pathLength.length);
            pathBFS(w);
            System.arraycopy(pathLength, 0, pathW, 0, pathLength.length);
            vertCache.get().put(v, pathV);
            vertCache.get().put(w, pathW);
        }
        return ancestorLength(pathV, pathW);
    }

    private int[] getAncestorData(Iterable<Integer> v, Iterable<Integer> w) {
        if (iterableCache != null && iterableCache.get() != null) {
            Integer[] cacheV = iterableCache.get().get(v);
            if (cacheV == null) {
                pathBFS(v, true);
                System.arraycopy(pathLength, 0, pathV, 0, pathLength.length);
                // add new cache data
                iterableCache.get().put(v, Arrays.copyOfRange(pathLength, 0, pathLength.length));
            }
            else {
                System.arraycopy(cacheV, 0, pathV, 0, pathLength.length);
                ancestorsV.clear();
                for (int i = 0; i < cacheV.length; i++)
                    if (cacheV[i] != null)
                        ancestorsV.add(i);
            }

            Integer[] cacheW = iterableCache.get().get(w);
            if (cacheW == null) {
                pathBFS(w, false);
                System.arraycopy(pathLength, 0, pathV, 0, pathLength.length);
                iterableCache.get().put(w, Arrays.copyOfRange(pathLength, 0, pathLength.length));
            }
            else {
                System.arraycopy(cacheW, 0, pathW, 0, pathLength.length);
                ancestorsW.clear();
                for (int i = 0; i < cacheW.length; i++)
                    if (cacheW[i] != null)
                        ancestorsW.add(i);
            }
        }
        else {
            iterableCache = new SoftReference<HashMap<Iterable<Integer>, Integer[]>>(
                    new HashMap<Iterable<Integer>, Integer[]>());
            pathBFS(v, true);
            System.arraycopy(pathLength, 0, pathV, 0, pathLength.length);
            pathBFS(w, false);
            System.arraycopy(pathLength, 0, pathW, 0, pathLength.length);
            iterableCache.get().put(v, Arrays.copyOfRange(pathV, 0, pathV.length));
            iterableCache.get().put(w, Arrays.copyOfRange(pathW, 0, pathW.length));
        }
        return ancestorLength(pathV, pathW);
    }


    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

}
