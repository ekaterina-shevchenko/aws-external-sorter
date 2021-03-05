package de.tum.service.workers;

import de.tum.cluster.ClusterMember;
import de.tum.data.Record;
import de.tum.service.ClusterService;
import de.tum.service.SelfDataProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DataRouterWorker extends Thread {
    private final BlockingQueue<Record> queue;
    private final ClusterService clusterService;
    private final SelfDataProcessor selfDataProcessor;
    private final ExecutorService executor;

    public DataRouterWorker(SelfDataProcessor selfDataProcessor, ClusterService clusterService, BlockingQueue<Record> queue, int poolSize) {
        this.selfDataProcessor = selfDataProcessor;
        this.clusterService = clusterService;
        this.queue = queue;
        this.executor = Executors.newFixedThreadPool(poolSize);
        super.setName("Data-Router-Thread");
    }


    @Override
    public void run() {
        try {
            while(true) {
                Record record = queue.take();
                ClusterMember member = clusterService.getMemberByOffset(record.getKey());
                if (member == null) { //record is ours
                    selfDataProcessor.addToQueue(record);
                } else {
                    executor.submit(() -> member.sendRecordToMember(record));
                }
            }
        } catch (InterruptedException e) {
            log.info("Received termination command. No record is expected now. Shutting down SelfDataProcessor");
            selfDataProcessor.shutdownWorker();
        } catch (Exception e) {
            log.info("Caught unexpected exception. Shutting down", e);
        }
    }
}
