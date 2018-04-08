package bio;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReaderFactory;

import java.nio.file.Path;

public class CollectMappedReadsMetrics {

    public static void main(String[] args) {
        Path bamFile = Utils.getFileFromResources("sample1-small.bam");
        SAMRecordIterator iterator = SamReaderFactory.makeDefault().open(bamFile).iterator();
        Metric metric = new Metric();
        while (iterator.hasNext()) {
            SAMRecord record = iterator.next();
            if (record.getReadUnmappedFlag()) {
                continue;
            }
            metric.inc(record.getReferenceName());
        }
        metric.printResult();
    }

}
