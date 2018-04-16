package bio.variation;

import java.util.List;

class Variation {
    private String refAllele;
    private List<Allele> alleles;
    private String genotype;
    private VariationType type;
    private String sequenceName;
    private int position;
    private int readCoverage;

    public Variation(String refAllele, List<Allele> alleles, String genotype,
                     VariationType type, String sequenceName, int position, int readCoverage) {
        this.refAllele = refAllele;
        this.alleles = alleles;
        this.genotype = genotype;
        this.type = type;
        this.sequenceName = sequenceName;
        this.position = position;
        this.readCoverage = readCoverage;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public List<Allele> getAlleles() {
        return alleles;
    }

    public String getGenotype() {
        return genotype;
    }

    public VariationType getType() {
        return type;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public int getPosition() {
        return position;
    }

    public int getReadCoverage() {
        return readCoverage;
    }
}
