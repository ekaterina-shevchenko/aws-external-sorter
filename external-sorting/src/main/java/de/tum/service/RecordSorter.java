package de.tum.service;

import de.tum.service.workers.RecordSorterWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordSorter implements Observer {
    @Autowired
    private DataUploader uploader;
    private RecordSorterWorker worker;

    public void shutdownWorker(){
        worker.interrupt();
    }

    @Override
    public void observe() {
        worker = new RecordSorterWorker(uploader);
        worker.start();
    }
}
