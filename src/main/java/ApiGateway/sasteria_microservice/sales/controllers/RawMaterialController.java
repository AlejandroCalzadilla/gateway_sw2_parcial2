package ApiGateway.sasteria_microservice.sales.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RestController
@RequestMapping("/graphql")
public class RawMaterialController {


    private final DiscoveryClient discoveryClient;

    @Autowired
    public RawMaterialController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostMapping
    public Mono<Map<String, Object>> handleGraphQLRequest(@RequestBody Map<String, Object> request) {
        String query = request.get("query").toString();

        if (query.contains("createRawMaterial")) {
            return sendRequestToService("sastreria", query);
        } else if (query.contains("findAllRawMaterials")) {
            return sendRequestToService("sastreria", query);
        } else if (query.contains("findRawMaterialById")) {
            return sendRequestToService("sastreria", query);
        } else if (query.contains("updateRawMaterial")) {
            return sendRequestToService("sastreria", query);
        } else if (query.contains("deleteRawMaterial")) {
            return sendRequestToService("sastreria", query);
        }

        return Mono.empty();
    }

    private Mono<Map<String, Object>> sendRequestToService(String serviceName, String query) {
        String serviceUrl = discoveryClient.getInstances(serviceName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not available"))
                .getUri()
                .toString();

        return WebClient.create(serviceUrl)
                .post()
                .uri("/graphql")
                .bodyValue(Map.of("query", query))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}