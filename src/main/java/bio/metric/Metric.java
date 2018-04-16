package bio.metric;

import java.util.HashMap;
import java.util.Map;

public class Metric {
    private Map<String, Long> chromosomeCounts;

    public Metric() {
        this.chromosomeCounts = new HashMap<>();
    }

    public void inc(String chr) {
        chromosomeCounts.compute(chr, (key, count) -> count == null ? 0 : count + 1);
    }

    public void addStats(String chr, Long count) {
        chromosomeCounts.compute(chr, (key, old) -> old == null ? count: old + count);
    }

    public void printResult() {
        chromosomeCounts.forEach((chr, count) -> System.out.println(chr + ": " + count));
    }

    public Map<String, Long> getChromosomeCounts() {
        return chromosomeCounts;
    }
}
