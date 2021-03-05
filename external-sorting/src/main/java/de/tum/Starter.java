package de.tum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;


@EnableAsync
@EnableWebSocket
@SpringBootApplication
@EnableConfigurationProperties
public class Starter {
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Bean
    public RestTemplate restTemplateSpringBean() {
        return restTemplate;
    }

    @Bean
    public S3Client s3ClientSpringBean() {
        return S3Client.builder().credentialsProvider(DefaultCredentialsProvider.create()).build();
    }
}
