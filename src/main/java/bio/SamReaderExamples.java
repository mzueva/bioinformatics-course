package bio;

import htsjdk.samtools.*;

import java.nio.file.Path;

public class SamReaderExamples {

    public static final char TAB = '\t';

    public static void main(String[] args) {
        Path bamFile = Utils.getFileFromResources("sample1-small.bam");
        readFully(bamFile);
        readInterval(bamFile, "chr1", 1, 200_000_000);
    }

    private static void readInterval(Path bamFile, String chromosome, int start, int end) {
        SamReader reader = SamReaderFactory.makeDefault().open(bamFile);
        SAMRecordIterator intervalIterator = reader.query(chromosome, start, end, false);
        while (intervalIterator.hasNext()) {
            System.out.println(Utils.formatSamRecord(intervalIterator.next()));
        }
    }

    private static void readFully(Path bamFile) {
        SamReader reader = SamReaderFactory.makeDefault().open(bamFile);
        SAMFileHeader fileHeader = reader.getFileHeader();
        fileHeader.getSequenceDictionary().getSequences().forEach(System.out::println);
        SAMRecordIterator fullFileIterator = reader.iterator();
        while (fullFileIterator.hasNext()) {
            System.out.println(Utils.formatSamRecord(fullFileIterator.next()));
        }

    }

}
