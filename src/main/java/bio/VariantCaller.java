package bio;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFileWalker;
import htsjdk.samtools.util.SamLocusIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class VariantCaller {

    public static final String REF = "REF";

    public static void main(String[] args) {
        Path bamFile = Utils.getFileFromResources("sample1-small.bam");
        SamReader samReader = SamReaderFactory.makeDefault().open(bamFile);
        SamLocusIterator samLocusIterator = new SamLocusIterator(samReader);
        ReferenceSequenceFileWalker referenceWalker =
                new ReferenceSequenceFileWalker(Paths.get("C:\\Users\\vpk\\Downloads\\GRCh38.primary_assembly.genome.fa"));
        while (samLocusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo = samLocusIterator.next();
            if (locusInfo.size() < 10) {
                continue;
            }
            ReferenceSequence referenceSequence = referenceWalker.get(locusInfo.getSequenceIndex());
            Variation variation = callVariants(locusInfo, referenceSequence.getBases()[locusInfo.getPosition() - 1]);
            if (variation != null && !REF.equals(variation.getType())) {
                System.out.println(variation);
            }
        }
    }

    private static Variation callVariants(SamLocusIterator.LocusInfo locusInfo, byte refBase) {
        Map<String, Integer> alleleCount = new HashMap<>();
        int totalCount = locusInfo.size();
        for (SamLocusIterator.RecordAndOffset recordAndOffset : locusInfo.getRecordAndOffsets()) {
            alleleCount
                    .compute(Utils.byteToString(recordAndOffset.getReadBase()), (key, count) -> count == null ? 1 : count + 1);
        }
        String referenceAllele = Utils.byteToString(refBase);

        if (alleleCount.size() > 2) {
            return new Variation(referenceAllele, new ArrayList<>(alleleCount.keySet()), "", "NO_CALL");
        }
        if (alleleCount.size() == 1 && alleleCount.containsKey(referenceAllele)) {
            return new Variation(referenceAllele, Collections.singletonList(referenceAllele), "", REF);
        }
        return new Variation(referenceAllele, new ArrayList<>(alleleCount.keySet()), "", "SNP");
    }

    private static class Variation {
        private String refAllele;
        private List<String> alleles;
        private String genotype;
        // REF, SNP, NO_CALL
        private String type;

        public Variation(String refAllele, List<String> alleles, String genotype, String type) {
            this.refAllele = refAllele;
            this.alleles = alleles;
            this.genotype = genotype;
            this.type = type;
        }

        public String getRefAllele() {
            return refAllele;
        }

        public List<String> getAlleles() {
            return alleles;
        }

        public String getGenotype() {
            return genotype;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Variation{" +
                    "refAllele='" + refAllele + '\'' +
                    ", alleles=" + alleles +
                    ", genotype='" + genotype + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
