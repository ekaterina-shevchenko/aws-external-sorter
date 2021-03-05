package de.tum.service.workers;

import de.tum.data.Record;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RecordWriter extends Thread{
    private static final AtomicInteger fileNumber = new AtomicInteger(0);

    private final BlockingQueue<Record> queue;
    private final Integer recordsInFile;

    static {
        new File("temp/unsorted/").mkdirs();
        new File("temp/received/").mkdirs();
    }

    public RecordWriter(BlockingQueue<Record> queue, Integer recordsInFile) {
        this.queue = queue;
        this.recordsInFile = recordsInFile;
        super.setName("Record-Writer-Thread");
    }

    @Override
    @SneakyThrows
    public void run() {
        BufferedOutputStream outputStream = null;
        File newFile = null;
        try {
            while(true) {
                int threadLocalFileNumber = fileNumber.getAndIncrement();
                log.info("Creating new received file {} ", threadLocalFileNumber);
                newFile = new File("temp/received/" + threadLocalFileNumber);
                newFile.createNewFile();
                outputStream = new BufferedOutputStream(new FileOutputStream(newFile));
                log.info("Writing records to the file");
                int index = 0;
                while(index < recordsInFile) {
                    outputStream.write(queue.take().getByteArray()); // On the last (incomplete) file interrupt occurs
                    index++;
                }
                outputStream.close();
                log.info("File {} has been formed", threadLocalFileNumber);
                moveFile(newFile, "temp/unsorted/");
                log.info("File has been moved to unsorted dir");
            }
        } catch (InterruptedException e) {
            outputStream.close(); // close the last file's output stream
            log.info("All data has been written to files. Queue should be empty {}", queue.size());
            moveFile(newFile, "temp/unsorted/");
            log.info("File has been moved to unsorted dir");
        } catch (Exception e) {
            log.error("Error while writing to file", e);
        }
    }

    private void moveFile(File file, String destDir){
        file.renameTo(new File(destDir + file.getName()));
    }
}
