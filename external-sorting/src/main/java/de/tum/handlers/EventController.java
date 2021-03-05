package de.tum.handlers;

import de.tum.service.DataRouter;
import de.tum.service.DataUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class EventController {
    @Autowired
    private DataRouter dataRouter;
    @Autowired
    private DataUploader dataUploader;

    @PostMapping("/uploadId")
    public Map<String, String> receiveUploadIdEvent(@RequestBody Map<String, String> request) {
        dataRouter.setUploadId(request.get("uploadId"));
        log.info("Got uploadId request {}", request.get("uploadId"));
        return Collections.singletonMap("success","true");
    }
    @PostMapping("/terminate")
    public Map<String, String> receiveTerminateEvent() {
        log.info("Got terminate request");
        dataRouter.terminate();
        return Collections.singletonMap("success","true");
    }
    @PostMapping("/etag")
    public Map<String, String> receiveETagEvent(@RequestBody List<CompletedPart> request) {
        dataUploader.receiveCompletedParts(request);
        log.info("Got ETags request {}", request);
        return Collections.singletonMap("success","true");
    }
}
