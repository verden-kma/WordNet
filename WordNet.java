import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WordNet {

    private final Map<String, LinkedList<Integer>> indeciesMap = new TreeMap<>();
    private final Map<Integer, String> synsetsMap = new TreeMap<>();
    private final Digraph hypernymsGraph;
    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) throw new IllegalArgumentException();

        // readSynsets
        In inputSynset = new In(synsets);
        while (inputSynset.hasNextLine()) {
            String line = inputSynset.readLine();
            String[] fields = line.split(",");

            for (String s : fields[1].split(" ")) {
                List<Integer> occurances = indeciesMap.get(s);
                if (occurances == null) {
                    LinkedList<Integer> list = new LinkedList<>();
                    list.add(Integer.parseInt(fields[0]));
                    indeciesMap.put(s, list);
                }
                else {
                    occurances.add(Integer.parseInt(fields[0]));
                }
            }
            synsetsMap.put(Integer.parseInt(fields[0]), fields[1]);
        }
        inputSynset.close();

        // readHypernyms
        In inputHypernym = new In(hypernyms);
        hypernymsGraph = new Digraph(synsetsMap.size());

        while (inputHypernym.hasNextLine()) {
            String line = inputHypernym.readLine();
            String fields[] = line.split(",");
            for (int i = 1; i < fields.length; i++) {
                hypernymsGraph.addEdge(Integer.parseInt(fields[0]), Integer.parseInt(fields[i]));
            }
        }
        inputHypernym.close();

        DirectedCycle dc = new DirectedCycle(hypernymsGraph);
        // DiCycle dc = new DiCycle(hypernymsGraph);
        if (dc.hasCycle()) throw new IllegalArgumentException();

        checkForSingleRoot();

        // use fully constructed graph to create a SAP object
        sap = new SAP(hypernymsGraph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return indeciesMap.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException();
        // if there is no such word, null will be returned
        return indeciesMap.get(word) != null;
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        validateInput(nounA, nounB);
        return sap.length(indeciesMap.get(nounA), indeciesMap.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        validateInput(nounA, nounB);
        int ancestor = sap.ancestor(indeciesMap.get(nounA), indeciesMap.get(nounB));
        return synsetsMap.get(ancestor);
    }

    private void validateInput(String nounA, String nounB) {
        if (nounA == null || nounB == null) throw new IllegalArgumentException();
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException();
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        System.out.println(wordnet.distance("damage", "jump"));
    }

    private void checkForSingleRoot() {
        // there is only one vertex that is allowed to have no adjacent verticies - the root
        // once the root is found, the other adjesentless vertex indiates incorrect input
        boolean rootFound = false;
        for (int i = 0; i < hypernymsGraph.V(); i++) {
            if (!hypernymsGraph.adj(i).iterator().hasNext()) {
                if (rootFound) throw new IllegalArgumentException();
                else rootFound = true;
            }
        }
    }
}
