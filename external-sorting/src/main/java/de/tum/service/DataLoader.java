package de.tum.service;

import java.io.IOException;
import java.io.InputStream;

import de.tum.utils.RecordUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTorrentRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Slf4j
@Component
@DependsOn("clusterService")
public class DataLoader implements Observer {
    @Autowired
    S3Client s3Client;
    @Autowired
    private DataRouter dataRouter;
    @Autowired
    private ClusterService clusterService;
    @Value("${aws.download.bucket}")
    private String bucket;
    @Value("${aws.download.key}")
    private String key;

    public void startDownload() throws IOException {
        HeadObjectResponse objectMetadata = s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucket).key(key).build());
        long length = objectMetadata.contentLength();
        if (length % 100 != 0) {
            log.error("File does not contain whole records");
            System.exit(-1);
        }
        long totalRecordsInFile = length / 100;

        int ownOffset = clusterService.getClusterInstanceOffset();
        long startRange = (totalRecordsInFile / clusterService.getNumberOfMembers()) * ownOffset * 100;
        long endRange = (totalRecordsInFile / clusterService.getNumberOfMembers()) * (ownOffset + 1) * 100 - 1;
        if (clusterService.isLastMember()) {
            endRange = length;
        }
        log.info("Instance starts downloading file from {} to {}", startRange, endRange);
        String range = String.format("bytes=%d-%d", startRange, endRange);
        GetObjectRequest rangeObjectRequest = GetObjectRequest.builder()
                .bucket(bucket).key(key).range(range).build(); // set range
        ResponseInputStream<GetObjectResponse> objectStream = s3Client.getObject(rangeObjectRequest);
        RecordUtils.consumeRecordsInStream(objectStream, dataRouter::addToQueue);
    }

    @SneakyThrows
    @Override
    @Async
    public void observe() {
        try {
            log.info("Starting downloading the file");
            startDownload();
            log.info("File downloading has been completed. Sending terminate command to all instances");
            clusterService.postToMembers("/terminate", null);
            dataRouter.terminate();
        } catch (Exception e) {
            log.error("Unknown error occurred ", e);
            throw e;
        }
    }
}
