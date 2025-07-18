package ru.practicum.ewm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatHitDto;
import ru.practicum.dto.StatViewDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StatClientImpl implements StatClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DiscoveryClient discoveryClient;
    private final String statServerId;
    private  final RestClient restClient;
    private final ObjectMapper mapper;

    public StatClientImpl(ObjectMapper mapper, DiscoveryClient discoveryClient,
                          @Value("${stat-service.stat-server-id}") String statServerId) {
        this.mapper = mapper;
        this.restClient = RestClient.builder().build();
        this.discoveryClient = discoveryClient;
        this.statServerId = statServerId;
    }

    public void hit(StatHitDto statHitDto) {
        String jsonBody;
        String statServerUri = getStatServerId();
        try {
            jsonBody = mapper.writeValueAsString(statHitDto);
            restClient.post()
                    .uri(statServerUri + "/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Ошибка при записи hit", ex);
            throw new RuntimeException("Ошибка при записи hit: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<StatViewDto> getStat(LocalDateTime start, LocalDateTime end,
                                     List<String> uris, Boolean unique) {
        String statServerUri = getStatServerId();
        System.out.println(statServerUri);
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                    .path("/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", uris);
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String uri = builder.build().toUriString();

            return restClient.get()
                    .uri(statServerUri + uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatViewDto>>() {
                    });
        } catch (RestClientException e) {
            log.error("Ошибка при запросе на получение статистики", e);
            return new ArrayList<>();
        }
    }

    private String getStatServerId() {
        List<ServiceInstance> instances = discoveryClient.getInstances(statServerId);
        if (instances.isEmpty()) {
            throw new IllegalStateException("Адрес сервера статистики не найден");
        }
        return instances.getFirst().getUri().toString();
    }
}
