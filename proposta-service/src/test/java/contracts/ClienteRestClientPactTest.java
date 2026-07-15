package contracts;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.HttpInteractionBuilder;
import au.com.dius.pact.consumer.dsl.HttpRequestBuilder;
import au.com.dius.pact.consumer.dsl.HttpResponseBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import dtos.responses.DadosClienteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import kotlin.jvm.functions.Function1;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PactConsumerTest
@PactTestFor(providerName = "cliente-service", pactMethod = "clienteServiceDeveResponderComDadosDoCliente")
class ClienteRestClientPactTest {

    private static final String CPF = "12345678900";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Pact(consumer = "proposta-service", provider = "cliente-service")
    V4Pact clienteServiceDeveResponderComDadosDoCliente(PactBuilder builder) {
        builder.expectsToReceiveHttpInteraction("uma consulta de cliente por CPF", new Function1<HttpInteractionBuilder, HttpInteractionBuilder>() {
            @Override
            public HttpInteractionBuilder invoke(HttpInteractionBuilder interaction) {
                interaction.withRequest(new Function1<HttpRequestBuilder, HttpRequestBuilder>() {
                    @Override
                    public HttpRequestBuilder invoke(HttpRequestBuilder request) {
                        return request
                                .method("GET")
                                .path("/clientes/" + CPF);
                    }
                }).willRespondWith(new Function1<HttpResponseBuilder, HttpResponseBuilder>() {
                    @Override
                    public HttpResponseBuilder invoke(HttpResponseBuilder response) {
                        return response
                                .status(200)
                                .headers(Map.of("Content-Type", "application/json"))
                                .body(new PactDslJsonBody()
                                        .equalTo("cpf", CPF)
                                        .equalTo("nome", "Maria Silva")
                                        .equalTo("ocupacao", "Analista de Sistemas")
                                        .decimalType("renda", new BigDecimal("7500.50")));
                    }
                });
                return interaction;
            }
        });

        return builder.toPact();
    }

    @Test
    void deveConsumirOContratoDoClienteService(MockServer mockServer) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mockServer.getUrl() + "/clientes/" + CPF))
                .GET()
                .build();

        HttpResponse<String> responseHttp = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();

        DadosClienteResponse response;
        try {
            response = OBJECT_MAPPER.readValue(responseHttp.body(), DadosClienteResponse.class);
        } catch (Exception exception) {
            throw new RuntimeException("Falha ao converter a resposta do contrato para DadosClienteResponse", exception);
        }

        assertEquals(200, responseHttp.statusCode());
        assertEquals(CPF, response.cpf());
        assertEquals("Maria Silva", response.nome());
        assertEquals("Analista de Sistemas", response.ocupacao());
        assertEquals(new BigDecimal("7500.50"), response.renda());
    }
}