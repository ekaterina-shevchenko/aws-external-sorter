package de.tum.utils;

import de.tum.data.FileRecord;
import de.tum.data.Record;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

public class RecordUtils {
    private static final Integer EOF = -1;

    private RecordUtils() {}

    public static void consumeRecordsInStream(InputStream input, Consumer<Record> recordConsumer) throws IOException {
        byte[] buffer = new byte[10000];
        while((input.read(buffer)) > 0) {
            int offset = 0;
            while(offset + 100 <= buffer.length) {
                Record record = new Record(Arrays.copyOfRange(buffer,offset,offset + 10),
                        Arrays.copyOfRange(buffer,offset + 10,offset + 100));
                recordConsumer.accept(record);
                offset+=100;
            }
        }
        input.close();
    }

    @SneakyThrows
    public static boolean consumeFileRecord(FileInputStream input, Consumer<FileRecord> recordConsumer){
        byte[] buffer = new byte[100];
        int offset = 0;
        if (input.read(buffer) != EOF) {
            FileRecord record = new FileRecord(Arrays.copyOfRange(buffer, offset, offset + 10),
                    Arrays.copyOfRange(buffer, offset + 10, offset + 100), input);
            recordConsumer.accept(record);
            return true;
        }
        return false;
    }
}
