package bio.variation;

import bio.Utils;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFileWalker;
import htsjdk.samtools.util.SamLocusIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Mpileup {

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
            String line = formatLocusInfo(locusInfo, referenceSequence.getBases()[locusInfo.getPosition() - 1]);
            if (line != null) {
                System.out.println(line);
            }
        }
    }

    private static String formatLocusInfo(SamLocusIterator.LocusInfo locusInfo, byte refBase) {
        StringBuilder result = new StringBuilder()
                .append(locusInfo.getSequenceName())
                .append("\t")
                .append(locusInfo.getPosition())
                .append("\t")
                .append(locusInfo.size())
                .append("\t")
                .append(Utils.byteToString(refBase))
                .append("\t");
        List<SamLocusIterator.RecordAndOffset> recordAndOffsets = locusInfo.getRecordAndOffsets();
        boolean allMatch = true;
        StringBuilder bases = new StringBuilder();
        for (SamLocusIterator.RecordAndOffset recordAndOffset : recordAndOffsets) {
            if (recordAndOffset.getReadBase() == refBase) {
                bases.append(".");
            } else {
                allMatch = false;
                bases.append(Utils.byteToString(recordAndOffset.getReadBase()));
            }

        }
        if (!allMatch) {
            result.append(bases);
            return result.toString();
        } else {
            return null;
        }
    }

}
