package bio;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CollectMappedReadsMetricsParallel {

    public static void main(String[] args) {
        Path bamFile = Utils.getFileFromResources("sample1-small.bam");
        SamReader samReader = SamReaderFactory.makeDefault().open(bamFile);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

        Metric resultMetrics = samReader.getFileHeader().getSequenceDictionary().getSequences()
                .stream()
                .map(sequence -> CompletableFuture.supplyAsync(() -> {
                    SamReader inner = SamReaderFactory.makeDefault().open(bamFile);
                    SAMRecordIterator iterator = inner.query(sequence.getSequenceName(), 0, -1, true);
                    Metric metric = new Metric();
                    while (iterator.hasNext()) {
                        SAMRecord record = iterator.next();
                        if (record.getReadUnmappedFlag()) {
                            continue;
                        }
                        metric.inc(record.getReferenceName());
                    }
                    return metric; }, executor))
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        System.err.println(e.getMessage());
                        return null;
                    } })
                .reduce(new Metric(), (m1, m2) -> {
                    if (!m2.getChromosomeCounts().isEmpty()) {
                        m2.getChromosomeCounts().forEach(m1::addStats);
                    }
                    return m1;
                });


        resultMetrics.printResult();
    }
}
