package de.tum.service.workers;

import de.tum.utils.RecordUtils;
import de.tum.data.FileRecord;
import de.tum.data.Record;
import de.tum.data.RecordComparator;
import de.tum.service.DataUploader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class RecordSorterWorker extends Thread{
    private final File unsortedFolder = new File("temp/unsorted/");
    private final File sortedFolder = new File("temp/sorted");
    private DataUploader uploader;
    private ExecutorService sortingPool;
    private Set<Future> futures = new HashSet<>();

    public RecordSorterWorker(DataUploader uploader) {
        this.uploader = uploader;
        unsortedFolder.mkdirs();
        sortedFolder.mkdirs();
        super.setName("Data-Sorter-Thread");
        ThreadFactory threadFactory = new CustomizableThreadFactory("Record-Sorter-Slave");
        sortingPool = Executors.newFixedThreadPool(3, threadFactory);
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            while (true) {
                File[] files = unsortedFolder.listFiles();
                if (files.length > 0) {
                    for (File file : files) {
                        log.info("Start sorting file {}", file.getName());
                        futures.add(sortingPool.submit(() -> sortFile(file)));
                    }
                    log.info("Waiting for threads to finish");
                    for (Future future : futures) {
                        future.get();
                    }
                    futures.clear();
                } else {
                    if (Thread.currentThread().isInterrupted()) {
                        doWhenInterrupter();
                    } else {
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (InterruptedException e) {
            doWhenInterrupter();
        } catch (Exception e) {
            log.error("Error" ,e);
            throw e;
        }
    }

    @SneakyThrows
    private void doWhenInterrupter() {
        uploader.getWorker().start();
        log.info("Waiting for threads to finish");
        for (Future future : futures) {
            future.get();
        }
        log.info("All threads are done. Running external sort");
        doExternalSort();
        uploader.getInputStream().close();
    }

    @SneakyThrows
    private void sortFile(File file) {
        String filename = file.getName();
        File sortedFile = new File("temp/sorted/" + filename);
        sortedFile.createNewFile();
        FileInputStream input = new FileInputStream(file);
        List<Record> records = new LinkedList<>();
        RecordUtils.consumeRecordsInStream(input, records::add);
        records.sort(new RecordComparator());
        FileOutputStream output = new FileOutputStream(sortedFile);
        for (Record record : records) {
            output.write(record.getByteArray());
        }
        input.close();
        output.close();
        file.delete();
    }

    @SneakyThrows
    private void doExternalSort() {
        List<FileInputStream> activeFileInputStreams = new ArrayList<>();
        File[] files = sortedFolder.listFiles();
        for (File file: files) {
            FileInputStream input = new FileInputStream(file);
            activeFileInputStreams.add(input);
        }
        TreeSet<FileRecord> records = takeFirstRecords(activeFileInputStreams);
        doSort(activeFileInputStreams, records);
    }

    private TreeSet<FileRecord> takeFirstRecords(List<FileInputStream> activeFileInputStreams) throws IOException {
        TreeSet<FileRecord> records = new TreeSet<>(new RecordComparator());
        boolean successfulRead;
        for (FileInputStream input : activeFileInputStreams) {
            successfulRead = RecordUtils.consumeFileRecord(input, records::add);
            if (!successfulRead){
                activeFileInputStreams.remove(input);
                input.close();
            }
        }
        return records;
    }

    private void doSort(List<FileInputStream> activeFileInputStreams,
                         TreeSet<FileRecord> records) throws IOException {
        FileRecord minRecord;
        boolean successfulRead;
        while(true) {
            minRecord = records.first();
            uploader.addToStream(minRecord.getByteArray());
            FileInputStream input = minRecord.getFileInputStream();
            if (activeFileInputStreams.contains(input)) {
                successfulRead = RecordUtils.consumeFileRecord(input, records::add);
                if (!successfulRead){
                    activeFileInputStreams.remove(input);
                    input.close();
                }
            }
            records.remove(minRecord);
            if (activeFileInputStreams.isEmpty() && records.isEmpty()){
                break;
            }
        }
    }
}
