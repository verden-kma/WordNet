import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
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

    private SoftReference<HashMap<Iterable<Integer>, IntPair[]>> iterableCache;

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
        LinkedList<Integer> iterV = new LinkedList<>();
        iterV.add(v);
        LinkedList<Integer> iterW = new LinkedList<>();
        iterV.add(w);
        return getAncestorData(iterV, iterW).getV2();
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (v < 0 || v > digraph.V() || w < 0 || w > digraph.V())
            throw new IllegalArgumentException();
        LinkedList<Integer> iterV = new LinkedList<>();
        iterV.add(v);
        LinkedList<Integer> iterW = new LinkedList<>();
        iterV.add(w);
        return getAncestorData(iterV, iterW).getV1();
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
        for (Integer i : v)
            if (i == null) throw new IllegalArgumentException();
        for (Integer i : w)
            if (i == null) throw new IllegalArgumentException();
        return getAncestorData(v, w).getV2();

    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) throw new IllegalArgumentException();
        for (Integer i : v)
            if (i == null) throw new IllegalArgumentException();
        for (Integer i : w)
            if (i == null) throw new IllegalArgumentException();
        return getAncestorData(v, w).getV1();
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


    private IntPair ancestorLength(Integer[] pathV, Integer[] pathW) {
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
        return new IntPair(cca, ccap);
    }

    private void resetFields() {
        for (int i = 0; i < digraph.V(); i++) {
            marked[i] = false;
            pathLength[i] = null;
        }
    }

    private IntPair getAncestorData(Iterable<Integer> v, Iterable<Integer> w) {
        if (iterableCache != null && iterableCache.get() != null) {
            IntPair[] cacheV = iterableCache.get().get(v);
            if (cacheV == null) {
                exploreAndCache(v, true, pathV, ancestorsV);
            }
            else {
                loadCache(cacheV, pathV, ancestorsV);
            }

            IntPair[] cacheW = iterableCache.get().get(w);
            if (cacheW == null) {
                exploreAndCache(w, false, pathW, ancestorsW);
            }
            else {
                loadCache(cacheW, pathW, ancestorsW);
            }
        }
        else {
            iterableCache = new SoftReference<HashMap<Iterable<Integer>, IntPair[]>>
                    (new HashMap<Iterable<Integer>, IntPair[]>());

            exploreAndCache(v, true, pathV, ancestorsV);
            exploreAndCache(w, false, pathW, ancestorsW);
        }
        return ancestorLength(pathV, pathW);
    }

    private IntPair[] buildCacheItem(int size) {
        IntPair[] cache = new IntPair[size];
        int index = 0;
        for (int i = 0; i < pathLength.length; i++) {
            if (pathLength[i] != null) {
                cache[index++] = new IntPair(i, pathLength[i]);
                // no need to proceed if all possible intries are already added
                if (index == cache.length)
                    break;
            }
        }
        return cache;
    }

    private void loadCache(IntPair[] nonNull, Integer[] path, Set<Integer> ancestors) {
        ancestors.clear();
        for (int i = 0; i < path.length; i++) {
            path[i] = 0;
        }
        for (IntPair ip : nonNull) {
            path[ip.getV1()] = ip.getV2();
            ancestors.add(ip.getV1());
        }
    }

    private void exploreAndCache(Iterable<Integer> vetricies, boolean flag, Integer[] path, Set<Integer> ancestors) {
        pathBFS(vetricies, flag);
        System.arraycopy(pathLength, 0, path, 0, pathLength.length);
        IntPair[] cache = buildCacheItem(ancestors.size());
        iterableCache.get().put(vetricies, cache);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }

}

class IntPair {
    private int v1;
    private int v2;

    IntPair (int first, int second) {
        v1 = first;
        v2 = second;
    }

        int getV1() {
            return v1;
        }

        int getV2() {
            return v2;
        }
}
