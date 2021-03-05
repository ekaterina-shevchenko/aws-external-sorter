package de.tum.service;

import de.tum.data.Record;
import de.tum.service.workers.DataUploaderWorker;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@Component
public class DataUploader {
    @Autowired
    private S3Client s3Client;
    @Autowired
    private ClusterService clusterService;
    @Value("${aws.upload.bucket}")
    private String bucket;
    @Value("${aws.upload.key}")
    private String key;
    private BlockingQueue<Record> queue = new LinkedBlockingQueue<>(10000);
    private PipedInputStream inputStream = new PipedInputStream();
    private PipedOutputStream outputStream = new PipedOutputStream();
    private DataUploaderWorker worker = null;

    @PostConstruct
    public void initialize() throws IOException {
        outputStream.connect(inputStream);
        worker = new DataUploaderWorker(clusterService, inputStream, bucket, key, s3Client);
    }

    @Deprecated
    @SneakyThrows
    public void addToQueue(Record record) {
        queue.put(record);
    }

    public void addToStream(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    @PreDestroy
    public void shutdownWorker(){
        if (worker != null) {
            worker.interrupt();
        }
    }

    public void setUploadId(String uploadID){
        worker.setUploadId(uploadID);
    }

    public void receiveCompletedParts(List<CompletedPart> completedParts){
        worker.addCompletedPart(completedParts);
    }
}
