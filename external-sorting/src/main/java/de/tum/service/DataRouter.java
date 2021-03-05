package de.tum.service;

import de.tum.data.Record;
import de.tum.service.workers.DataRouterWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class DataRouter implements Observer {
    private final BlockingQueue<Record> queue = new LinkedBlockingQueue<>(1000);
    private DataRouterWorker worker;
    private Integer terminateCounter = 0;
    @Autowired
    private DataUploader uploader;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private SelfDataProcessor selfDataProcessor;
    @Value("${processor.worker.pool}")
    private Integer poolSize;

    @PostConstruct
    public void init() {
        worker = new DataRouterWorker(selfDataProcessor, clusterService, queue, poolSize);
    }

    @SneakyThrows
    public void addToQueue(Record record)  {
        queue.put(record);
    }

    public void terminate() {
        terminateCounter++;
        if (terminateCounter.equals(clusterService.getNumberOfMembers())) {
            shutdownWorker();
        }
    }

    public void setUploadId(String uploadId) {
        uploader.setUploadId(uploadId);
    }

    public void shutdownWorker() {
        worker.interrupt();
    }

    @Override
    public void observe() {
        worker.start();
    }
}
