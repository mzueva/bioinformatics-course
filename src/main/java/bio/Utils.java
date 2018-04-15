package bio;

import htsjdk.samtools.SAMRecord;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {

    public static Path getFileFromResources(String fileName) {
        URL resource = Utils.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Failed to find classpath resource: %s", fileName));
        }
        try {
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static String formatSamRecord(SAMRecord record) {
        return new StringBuilder()
                .append(record.getReadName())
                .append(SamReaderExamples.TAB)
                .append(record.getReferenceName())
                .append(':')
                .append(record.getAlignmentStart())
                .append('-')
                .append(record.getAlignmentEnd())
                .append(SamReaderExamples.TAB)
                .append(record.getCigar())
                .append(SamReaderExamples.TAB)
                .append(new String(record.getReadBases()))
                .append(SamReaderExamples.TAB)
                .append(record.getBaseQualityString()).toString();
    }

    public static String byteToString(byte readBase) {
        byte[] array = new byte[] {readBase};
        return new String(array);
    }
}
