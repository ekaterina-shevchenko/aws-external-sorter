package de.tum.service;

import de.tum.cluster.ClusterMember;
import de.tum.config.ClusterConfiguration;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;
import java.math.BigInteger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

@Slf4j
@Getter
@Component
public class ClusterService {

    private static final int IPV4_SIZE = 4;
    @Getter
    private Integer numberOfMembers;
    private ConcurrentMap<String, ClusterMember> members = new ConcurrentHashMap<>();
    @Autowired
    private ClusterConfiguration clusterConfiguration;
    @Autowired
    private WSClient wsClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private List<Observer> observers;

    @PostConstruct
    public void initCluster() throws IOException {
        List<String> instances = clusterConfiguration.getInstances();
        numberOfMembers = instances.size();
        String ownIP = getOwnIP(instances);
        log.info("Own IP has been found - {}", ownIP);
        List<String> connectIPs = instances.stream()
                .filter(instance -> ownIP.compareTo(instance.split(":")[0]) < 0)
                .collect(Collectors.toList());
        log.info("Should connect to {} instances: {}", connectIPs.size(), connectIPs);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        connectIPs.forEach(connectIP -> executorService.submit(() -> {
            try {
                wsClient.connect(connectIP);
            } catch (Exception e) {
                System.exit(-1);
            }
        }));
    }

    public void clusterFormed(){
        log.info("Notifying observers that cluster has been formed");
        observers.forEach(Observer::observe);
    }

    public void addSession(WebSocketSession session){
        members.put(session.getRemoteAddress().getAddress().getHostAddress(),
                new ClusterMember(session, getClusterInstanceOffset(session.getRemoteAddress().getAddress().getHostAddress())));
    }

    public void removeSession(WebSocketSession session){
        members.remove(session.getRemoteAddress().getAddress().getHostAddress());
    }

    private List<String> getLocalIPs() throws IOException {
        List<NetworkInterface> activeNetworkInterfaces =
                Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                        .filter(i -> {
                            try {
                                return i.isUp() && !i.isLoopback();
                            } catch (SocketException e) {
                                return false;
                            }
                        }).collect(Collectors.toList());
        List<String> ips = new ArrayList<>();
        for (NetworkInterface activeNetworkInterface : activeNetworkInterfaces) {
            Optional<String> ip = Collections.list(activeNetworkInterface.getInetAddresses())
                    .stream()
                    .filter(inetAddress -> inetAddress.getAddress().length == IPV4_SIZE)
                    .map(InetAddress::getHostAddress)
                    .findAny();
            ip.ifPresent(ips::add);
        }
        return ips;
    }

    private String getOwnIP(List<String> instances) throws IOException {
        List<String> ips = getLocalIPs();
        Optional<String> first = instances.stream()
                .map(instance -> instance.split(":")[0])
                .filter(ips::contains)
                .findFirst();
        if (first.isPresent()){
            return first.get();
        } else {
            throw new IllegalStateException("cluster.instances property does not contain own IP");
        }
    }

    public int getClusterInstanceOffset(String ip){
        List<String> instances = clusterConfiguration.getInstances().stream()
                .map(instance -> instance.split(":")[0]).collect(Collectors.toList());
        return instances.indexOf(ip);
    }

    @SneakyThrows
    public int getClusterInstanceOffset(){
        List<String> instances = clusterConfiguration.getInstances();
        String ownIP = getOwnIP(instances);
        return getClusterInstanceOffset(ownIP);
    }

    public ClusterMember getMemberByOffset(byte[] recordKey) {
        int firstBytes = new BigInteger(Arrays.copyOfRange(recordKey, 0, 4)).intValue();
        long firstBytesLong = Integer.toUnsignedLong(firstBytes);
        long maxInteger = (long) Integer.MAX_VALUE * (long) 2 + (long) 1;
        long offset = firstBytesLong / (maxInteger / numberOfMembers);
        if (offset > numberOfMembers -1) {
            offset = numberOfMembers -1;
        }
        long finalOffset = offset;
        return members.values().stream().filter(member -> member.getOffset() == finalOffset).findFirst().orElse(null);
    }

    @SneakyThrows
    public boolean isFirstMember() {
        List<String> instances = clusterConfiguration.getInstances();
        String ownIP = getOwnIP(instances);
        return ownIP.equals(instances.get(0).split(":")[0]);
    }

    public void postToMembers(String url, Map<String, String> requestBody){
        clusterConfiguration.getInstances()
                .forEach(ipPort -> restTemplate.postForEntity("http://" + ipPort + url, requestBody, String.class));
    }

    public void postToFirstMember(String url, Object requestBody) {
        String ipPort = clusterConfiguration.getInstances().get(0);
        restTemplate.postForEntity("http://" + ipPort + url, requestBody, String.class);
    }

    public boolean isLastMember() throws IOException {
        List<String> instances = clusterConfiguration.getInstances();
        String ownIP = getOwnIP(instances);
        return ownIP.equals(instances.get(numberOfMembers -1).split(":")[0]);
    }
}
