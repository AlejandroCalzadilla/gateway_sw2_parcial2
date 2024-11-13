package ApiGateway.sasteria_microservice.sales.controllers;



import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/graphql")
public class SatreriaController {

    private final DiscoveryClient discoveryClient;

    @Autowired
    public SatreriaController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostMapping
    public Mono<Map<String, Object>> handleGraphQLRequest(@RequestBody Map<String, Object> request, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String query = request.get("query").toString();
        return sendRequestToService("sastreria", query, authorizationHeader);
    }

    private Mono<Map<String, Object>> sendRequestToService(String serviceName, String query, String authorizationHeader) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new RuntimeException("Service not available");
        }

        String serviceUrl = instances.get(0).getUri().toString();

        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection())
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30))
                        .addHandlerLast(new WriteTimeoutHandler(30)));

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer size
                .baseUrl(serviceUrl)
                .build();

        WebClient.RequestBodySpec requestSpec = (WebClient.RequestBodySpec) webClient.post()
                .uri("/graphql")
                .bodyValue(Map.of("query", query));

        if (authorizationHeader != null) {
            requestSpec.header("Authorization", authorizationHeader);
        }

        return requestSpec.retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorMap(e -> new RuntimeException("Error communicating with service: " + e.getMessage(), e));
    }



    }







