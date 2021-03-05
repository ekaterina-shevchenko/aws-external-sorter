package de.tum.handlers;

import de.tum.data.Record;
import de.tum.service.ClusterService;
import de.tum.service.DataRouter;
import de.tum.service.WSClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import java.util.Arrays;

@Component
@Slf4j
public class WebSocketHandler extends BinaryWebSocketHandler {

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private DataRouter dataRouter;
    @Autowired
    private WSClient wsClient;


    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        Record record = getRecord(message);
        dataRouter.addToQueue(record);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New connection has been established with {}", session.getRemoteAddress());
        clusterService.addSession(session);
        if (wsClient.incCounterWithCheck()) {
            clusterService.clusterFormed();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection has been closed with {}", session.getRemoteAddress());
        clusterService.removeSession(session);
    }

    private Record getRecord(BinaryMessage message){
        byte[] messageBytes = message.getPayload().array();
        Record record = new Record(Arrays.copyOfRange(messageBytes, 0, 10),
                    Arrays.copyOfRange(messageBytes, 10, 100) );
        return record;
    }
}