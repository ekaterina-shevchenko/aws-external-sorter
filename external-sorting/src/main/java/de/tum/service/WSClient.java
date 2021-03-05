package de.tum.service;

import de.tum.handlers.WebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class WSClient {

    @Autowired
    private WebSocketHandler webSocketHandler;
    @Autowired
    private ClusterService clusterService;
    private final AtomicInteger handshakesCounter = new AtomicInteger(0);
    private final WebSocketClient webSocketClient = new StandardWebSocketClient();

    public void connect(String connectIpPort) throws Exception {
        while(true) {
            try {
                webSocketClient.doHandshake(webSocketHandler, new WebSocketHttpHeaders(),
                        URI.create("ws://" + connectIpPort + "/record")).get();
                break;
            } catch (ExecutionException e) {
                log.warn("Could not connect to instance {}. Retrying...", connectIpPort);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Unknown error while connecting to instance " + connectIpPort, e);
                throw e;
            }
        }
    }

    public boolean incCounterWithCheck() {
        return handshakesCounter.incrementAndGet() == (clusterService.getNumberOfMembers() - 1);
    }
}
