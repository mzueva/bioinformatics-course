package bio.variation;

public class Allele {

    private String base;
    private boolean reference;
    private int alleleDepth;
    private double alleleFrequency;

    public Allele(String base, boolean reference, int alleleDepth, double alleleFrequency) {
        this.base = base;
        this.reference = reference;
        this.alleleDepth = alleleDepth;
        this.alleleFrequency = alleleFrequency;
    }

    public String getBase() {
        return base;
    }

    public boolean isReference() {
        return reference;
    }

    public int getAlleleDepth() {
        return alleleDepth;
    }

    public double getAlleleFrequency() {
        return alleleFrequency;
    }

    @Override
    public String toString() {
        return "Allele{" +
                "base='" + base + '\'' +
                ", reference=" + reference +
                ", alleleDepth=" + alleleDepth +
                ", alleleFrequency=" + alleleFrequency +
                '}';
    }
}
