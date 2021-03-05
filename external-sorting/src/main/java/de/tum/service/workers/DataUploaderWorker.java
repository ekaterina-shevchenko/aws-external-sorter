package de.tum.service.workers;

import de.tum.service.ClusterService;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DataUploaderWorker extends Thread{
    private final Integer MIN_PART_SIZE = 5 * 1000 * 1000; // 5MB in bytes
    private static final long MAX_PART_SIZE = (long) (4.8 * 1000 * 1000 * 1000); // 4.8GB in bytes
    private final File folder = new File("temp/sorted");
    private final List<CompletedPart> completedParts = new ArrayList<>();
    private final AtomicInteger completedPartsCounter = new AtomicInteger(1);
    @Setter
    private volatile String uploadId = null;
    private final ClusterService clusterService;
    private final InputStream dataStream;
    private final String bucket;
    private final String key;
    private final S3Client s3Client;

    public DataUploaderWorker(ClusterService clusterService, InputStream dataStream, String bucket, String key, S3Client s3Client) {
        this.clusterService = clusterService;
        this.dataStream = dataStream;
        this.bucket = bucket;
        this.key = key;
        this.s3Client = s3Client;
        super.setName("Data-Uploader-Thread");
    }

    @SneakyThrows
    @Override
    public void run(){
        try {
            if (clusterService.isFirstMember()){
                log.info("The instance is the first in the list. Receiving and sending uploadId to cluster instances");
                uploadId = initializeUpload(s3Client);
                clusterService.postToMembers("/uploadId", Collections.singletonMap("uploadId", uploadId));
            } else {
                log.info("Waiting for UploadId from any other instance");
                while(true){
                    if (uploadId != null) {
                        break;
                    }
                    Thread.sleep(100);
                }
            }
            int partsNumber = getPartsNumber();
            log.info("Should send {} parts, except the last one", partsNumber);
            for (int part = 1; part < partsNumber; part++){ // 1 iteration less than partsNumber
                completedParts.add(uploadPart(part, MAX_PART_SIZE));
            }
            long lastPartSize = getLastPartSize();
            long correctedLastPartSize = lastPartSize < MIN_PART_SIZE ? lastPartSize + MAX_PART_SIZE: lastPartSize;
            completedParts.add(uploadPart(partsNumber, correctedLastPartSize));
            dataStream.close();
            log.info("All parts have been uploaded. Finalizing upload");
            finalizeUpload();

        } catch (InterruptedException e) {
            // quit gracefully
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    @SneakyThrows
    private void finalizeUpload(){
        if (clusterService.isFirstMember()){
            while(true){
                log.info("The instance is first. Waiting for ETags. Got {} ETags, waiting for {} in total",
                        completedPartsCounter.get(), clusterService.getNumberOfMembers());
                if (completedPartsCounter.get() == clusterService.getNumberOfMembers()) {
                    break;
                }
                Thread.sleep(1000);
            }
            log.info("All ETags have been collected. Sending CompleteMultipartUploadRequest");
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts).build();
            CompleteMultipartUploadRequest compRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();
            s3Client.completeMultipartUpload(compRequest);
        } else {
            log.info("The instance is not the first. Sending our ETags to the first instance");
            clusterService.postToFirstMember("/etag", completedParts);
        }
    }

    private CompletedPart uploadPart(int part, long partSize) {
        log.info("Uploading part {}", part);
        UploadPartRequest uploadRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partWithMemberOffset(part)) // part number is between 1 and 10000
                .contentLength(partSize).build();
        RequestBody body = RequestBody.fromInputStream(dataStream,partSize);
        UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadRequest, body);
        return CompletedPart.builder().eTag(uploadPartResponse.eTag()).partNumber(part).build();
    }

    private long getLastPartSize(){
        int allFilesWeight = getAllSortedFilesWeight();
        int remainingWeight = (int) (allFilesWeight % MAX_PART_SIZE);
        return remainingWeight;
    }

    private int getPartsNumber() {
        int allFilesWeight = getAllSortedFilesWeight();
        int uploadNumber = (int) (allFilesWeight / MAX_PART_SIZE);
        int remainingWeight = (int) (allFilesWeight % MAX_PART_SIZE);
        return remainingWeight < MIN_PART_SIZE ? uploadNumber: uploadNumber + 1;
    }

    private int getAllSortedFilesWeight() {
        List<File> files = Arrays.asList(folder.listFiles());
        return files.stream().map(file -> file.length()).reduce((l,r) -> l + r).get().intValue();
    }

    private int partWithMemberOffset(int part){
        int instanceStep = 1000;
        int beginningShift = clusterService.isFirstMember() ? 1 : 0;
        return clusterService.getClusterInstanceOffset() * instanceStep + beginningShift;
    }

    private String initializeUpload(S3Client s3Client) {
        CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build();
        return s3Client.createMultipartUpload(initRequest).uploadId();
    }

    public void addCompletedPart(List<CompletedPart> completedParts){
        this.completedParts.addAll(completedParts);
        completedPartsCounter.incrementAndGet();
    }
}
