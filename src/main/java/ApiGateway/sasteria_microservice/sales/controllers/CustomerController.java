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
@RequestMapping("/graphql/customer")
public class CustomerController {

    private final DiscoveryClient discoveryClient;

    @Autowired
    public CustomerController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @PostMapping
    public Mono<Map<String, Object>> handleGraphQLRequest(@RequestBody Map<String, Object> request) {
        String query = request.get("query").toString();
        return sendRequestToService("sastreria", query);
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