package de.tum.cluster;

import de.tum.data.Record;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

@Data
@RequiredArgsConstructor
public class ClusterMember {

    private final WebSocketSession webSocketSession;
    private final int offset;

    @SneakyThrows
    public void sendRecordToMember(Record record) {
        webSocketSession.sendMessage(new BinaryMessage(record.getByteArray()));
    }
}
