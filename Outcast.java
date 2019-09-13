
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {

    private final WordNet wn;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        wn = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        int outsider = 0;
        int maxDist = 0;
        int currentDist;
        for (int i = 0; i < nouns.length; i++) {
            currentDist = 0;
            for (int j = 0; j < nouns.length; j++) {
                currentDist += wn.distance(nouns[i], nouns[j]);
            }
            if (maxDist < currentDist) {
                outsider = i;
                maxDist = currentDist;
            }
        }
        return nouns[outsider];
    }

    private int fact(int n) {
        int f = 1;
        for (int i = 2; i <= n; i++)
            f *= i;
        return f;
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}