package de.tum.service;

import de.tum.data.Record;
import de.tum.service.workers.RecordWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class SelfDataProcessor implements Observer {
    private final BlockingQueue<Record> queue = new LinkedBlockingQueue<>(1000);

    @Value("${processor.writer.pool}")
    private Integer writerPool;
    @Value("${processor.records.file}")
    private Integer recordsInFile;
    private Set<RecordWriter> recordWriterCustomPool = new HashSet<>();
    @Autowired
    private RecordSorter recordSorter;


    public void addToQueue(Record record) throws InterruptedException {
        queue.put(record);
    }

    public void shutdownWorker() {
        log.info("Record writing has been finished. Running sorting process");
        recordWriterCustomPool.forEach(Thread::interrupt);
        recordSorter.shutdownWorker();
    }

    @Override
    public void observe() {
        for (int i = 0; i < writerPool; i++) {
            final RecordWriter writer = new RecordWriter(queue, recordsInFile);
            recordWriterCustomPool.add(writer);
            writer.start();
        }
    }
}
