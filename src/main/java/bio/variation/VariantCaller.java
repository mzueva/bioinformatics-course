package bio.variation;

import bio.Utils;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFileWalker;
import htsjdk.samtools.util.SamLocusIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a simple Variant Caller tool for finding SNP variations.
 * Expects path to bam file as first argument and path to reference fasta file as second command line argument.
 */
public class VariantCaller {

    private static final int MIN_COVERAGE = 10;
    private static final String MISSING_GENOTYPE = "";
    private static final double MIN_ALLELE_FREQUENCY = 0.1;
    private static final String REFERENCE_GENOTYPE = "0";
    private static final String ALT_GENOTYPE = "1";
    private static final double HETEROZYGOUS_THRESHOLD = 0.1;
    private static final double HOMOZYGOUS_THRESHOLD = 0.9;

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Expected two arguments: path to bam file and path to reference file.");
        }
        Path bamFile = Paths.get(args[0]);
        Path reference = Paths.get(args[1]);
        new VariantCaller().processFile(bamFile, reference);
    }

    public void processFile(Path bamFile, Path referenceFile) {
        SamReader samReader = SamReaderFactory.makeDefault().open(bamFile);
        SamLocusIterator samLocusIterator = new SamLocusIterator(samReader);
        ReferenceSequenceFileWalker referenceWalker = new ReferenceSequenceFileWalker(referenceFile);
        VariantWriter variantWriter = new VariantWriter();
        while (samLocusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo = samLocusIterator.next();
            if (locusInfo.size() < MIN_COVERAGE) {
                continue;
            }
            ReferenceSequence referenceSequence = referenceWalker.get(locusInfo.getSequenceIndex());
            Variation variation = callVariants(locusInfo, referenceSequence.getBases()[locusInfo.getPosition() - 1]);
            if (variation != null && variation.getType() != VariationType.REF) {
                variantWriter.print(variation);
            }
        }
    }

    private Variation callVariants(SamLocusIterator.LocusInfo locusInfo, byte refBase) {
        Map<String, Integer> alleleCount = new HashMap<>();
        int readDepth = locusInfo.size();
        for (SamLocusIterator.RecordAndOffset recordAndOffset : locusInfo.getRecordAndOffsets()) {
            alleleCount
                    .compute(Utils.byteToString(recordAndOffset.getReadBase()),
                            (key, count) -> count == null ? 1 : count + 1);
        }
        String referenceBase = Utils.byteToString(refBase).toUpperCase();

        List<Allele> filteredAlleles = alleleCount.entrySet().stream()
                .map(entry -> {
                    String alleleBase = entry.getKey();
                    Integer alleleDepth = entry.getValue();
                    return new Allele(alleleBase, alleleBase.equals(referenceBase), alleleDepth,
                            (double) alleleDepth / readDepth);
                })
                .filter(allele -> Double.compare(allele.getAlleleFrequency(), MIN_ALLELE_FREQUENCY) > 0)
                .collect(Collectors.toList());

        if (filteredAlleles.isEmpty()) {
            return null;
        }

        String genotype;
        VariationType type;

        if (filteredAlleles.size() > 2) {
            genotype = MISSING_GENOTYPE;
            type = VariationType.NO_CALL;
            return new Variation(referenceBase, filteredAlleles, genotype, type,  locusInfo.getSequenceName(),
                    locusInfo.getPosition(), readDepth);
        } else if (filteredAlleles.size() == 1) {
            Allele allele = filteredAlleles.get(0);
            genotype = allele.isReference() ? "0/0" : "1/1";
            type = allele.isReference() ? VariationType.REF : VariationType.SNP;
            return new Variation(referenceBase, filteredAlleles, genotype, type,  locusInfo.getSequenceName(),
                    locusInfo.getPosition(), readDepth);
        }

        Allele first = filteredAlleles.get(0);
        Allele second = filteredAlleles.get(1);

        if (isHeterozygous(first, second) || isHomozygous(first) || isHomozygous(second)) {
            genotype = buildGenotype(first, second);
            type = VariationType.SNP;
        }  else {
            genotype = MISSING_GENOTYPE;
            type = VariationType.NO_CALL;
        }

        return new Variation(referenceBase, filteredAlleles, genotype, type,  locusInfo.getSequenceName(),
                locusInfo.getPosition(), readDepth);
    }

    private String buildGenotype(Allele first, Allele second) {
        return getGenotypeCode(first) + "/" + getGenotypeCode(second);
    }

    private String getGenotypeCode(Allele allele) {
        return allele.isReference() ? REFERENCE_GENOTYPE : ALT_GENOTYPE;
    }

    private boolean isHeterozygous(Allele first, Allele second) {
        return isAlleleHeterozygous(first.getAlleleFrequency()) &&
                isAlleleHeterozygous(second.getAlleleFrequency());
    }

    private boolean isAlleleHeterozygous(double alleleFreq) {
        return Double.compare(alleleFreq, HETEROZYGOUS_THRESHOLD) > 0 &&
                Double.compare(alleleFreq, HOMOZYGOUS_THRESHOLD) < 0;
    }

    private boolean isHomozygous(Allele allele) {
        return Double.compare(allele.getAlleleFrequency(), HOMOZYGOUS_THRESHOLD) >= 0;
    }

}
