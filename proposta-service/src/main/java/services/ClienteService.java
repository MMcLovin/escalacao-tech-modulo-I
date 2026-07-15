package services;

import clients.ClienteRestClient;
import dtos.responses.DadosClienteResponse;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ClienteService {

    private static final Logger LOGGER = Logger.getLogger(ClienteService.class);

    private final ClienteRestClient restClient;

    public ClienteService(@RestClient ClienteRestClient restClient) {
        this.restClient = restClient;
    }

    // O quarkus por debaixo dos panos magicamente cuida de armazenar o resultado do método caso aconteça um cache miss, ou seja, o corpo do método anotado com cacheResult somente é executado em caso de cache miss, caso contrário o resultado é retornado diretamente do cache.
    @CacheResult(cacheName = "dados-cliente-cache")
    public Uni<DadosClienteResponse> buscarNoCache(String cpf) {
        LOGGER.infof("=== CACHE MISS para o CPF %s. Consultando cliente-service via HTTP ===", cpf);
        return restClient.obterDadosCliente(cpf);
    }

    public Uni<DadosClienteResponse> obterDadosCliente(String cpf) {
        return buscarNoCache(cpf)
                // Um cache miss é diferente de uma falha do serviço do redis, então caso o redis esteja indisponível por qualquer razão que seja, o método buscarNoCache irá falhar e o fallback (revocerWithItem) será acionado, chamando o serviço remoto do cliente na mão.
                .onFailure().recoverWithUni(
                        err -> {
                            LOGGER.warnf("Redis offline para o CPF %s. Motivo: %s. Executando fallback para API direta.", cpf, err.getMessage());
                            return restClient.obterDadosCliente(cpf);
                        }
                );
    }

    @CacheInvalidate(cacheName = "dados-cliente-cache")
    public Uni<Void> limparCacheCliente(String cpf) {
        LOGGER.infof("Invalidando cache para o CPF: %s", cpf);
        return Uni.createFrom().nullItem();
    }
}
