package ApiGateway.sasteria_microservice.sales.controllers;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/p")
public class PythonServiceController {

    private final DiscoveryClient discoveryClient;

    @Autowired
    public PythonServiceController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/hello")
    public Mono<Map> handleHelloRequest() {
        return sendRequestToService("pythonsastreria", "/hello", HttpMethod.GET, null);
    }

    @PostMapping("/transfer_data")
    public Mono<Map> handleTransferDataRequest(@RequestBody Map<String, Object> request) {
        return sendRequestToService("pythonsastreria", "/transfer_data", HttpMethod.POST, request);
    }

    @GetMapping("/kpi/ventas_totales_por_mes")
    public Mono<Map> handleVentasTotalesPorMesRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/ventas_totales_por_mes", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/cantidad_prendas_por_tipo")
    public Mono<Map> handleCantidadPrendasPorTipoRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/cantidad_prendas_por_tipo", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/estados_pedidos_actuales")
    public Mono<Map> handleEstadosPedidosActualesRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/estados_pedidos_actuales", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/materiales_mas_utilizados")
    public Mono<Map> handleMaterialesMasUtilizadosRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/materiales_mas_utilizados", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/pedidos_por_cliente")
    public Mono<Map> handlePedidosPorClienteRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/pedidos_por_cliente", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/cambios_realizados_por_mes")
    public Mono<Map> handleCambiosRealizadosPorMesRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/cambios_realizados_por_mes", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/edad_promedio_por_genero")
    public Mono<Map> handleEdadPromedioPorGeneroRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/edad_promedio_por_genero", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/costo_total_por_prenda")
    public Mono<Map> handleCostoTotalPorPrendaRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/costo_total_por_prenda", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/promedio_prendas_por_pedido")
    public Mono<Map> handlePromedioPrendasPorPedidoRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/promedio_prendas_por_pedido", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/total_ventas_mes_actual")
    public Mono<Map> handleTotalVentasMesActualRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/total_ventas_mes_actual", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/porcentaje_pedidos_completados")
    public Mono<Map> handlePorcentajePedidosCompletadosRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/porcentaje_pedidos_completados", HttpMethod.GET, null);
    }

    @GetMapping("/kpi/promedio_gasto_por_cliente")
    public Mono<Map> handlePromedioGastoPorClienteRequest() {
        return sendRequestToService("pythonsastreria", "/kpi/promedio_gasto_por_cliente", HttpMethod.GET, null);
    }

    private Mono<Map> sendRequestToService(String serviceName, String path, HttpMethod method, Map<String, Object> body) {
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
                .baseUrl(serviceUrl)
                .build();

        WebClient.RequestBodySpec requestSpec = (WebClient.RequestBodySpec) webClient.method(method)
                .uri(path);

        if (body != null) {
            requestSpec.bodyValue(body);
        }

        return requestSpec.retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new RuntimeException("Error communicating with service: " + e.getMessage(), e));
    }
}