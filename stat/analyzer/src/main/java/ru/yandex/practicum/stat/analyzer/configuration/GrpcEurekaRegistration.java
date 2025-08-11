package ru.yandex.practicum.stat.analyzer.configuration;

import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
public class GrpcEurekaRegistration {

    @Value("${grpc.server.port}")
    private int grpcPort;

    @Autowired
    private EurekaInstanceConfigBean eurekaInstanceConfig;

    @Autowired
    private EurekaClient eurekaClient;

    @EventListener
    public void onGrpcServerStarted(GrpcServerStartedEvent event) {
        int port = event.getPort();
        log.info("Updating gRPC port in Eureka: {}", port);
        eurekaInstanceConfig.getMetadataMap().put("grpc.port", String.valueOf(port));
        eurekaClient.getApplicationInfoManager().registerAppMetadata(eurekaInstanceConfig.getMetadataMap());
    }
}
