package de.tum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("cluster")
public class ClusterConfiguration {
    private List<String> instances;
}
