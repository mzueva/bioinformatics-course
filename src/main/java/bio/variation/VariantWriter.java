package bio.variation;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class VariantWriter {

    public static final String DELIMITER = "\t";

    public void print(Variation variation) {
        System.out.println(buildVariationLine(variation));
    }

    private String buildVariationLine(Variation variation) {
        return new StringBuilder()
                .append(variation.getSequenceName())
                .append(DELIMITER)
                .append(variation.getPosition())
                .append(DELIMITER)
                .append(variation.getRefAllele())
                .append(DELIMITER)
                .append(variation.getReadCoverage())
                .append(DELIMITER)
                .append(buildAlleles(variation.getAlleles()))
                .append(DELIMITER)
                .append(variation.getGenotype())
                .toString();
    }

    private String buildAlleles(List<Allele> alleles) {
        if (CollectionUtils.isEmpty(alleles)) {
            return "";
        }
        return alleles.stream().map(this::buildAllele).collect(Collectors.joining(","));
    }

    private String buildAllele(Allele allele) {
        return new StringBuilder()
                .append('[')
                .append(allele.getBase())
                .append(" AD=")
                .append(allele.getAlleleDepth())
                .append(" AF=")
                .append(allele.getAlleleFrequency()).append(']')
                .toString();
    }
}
