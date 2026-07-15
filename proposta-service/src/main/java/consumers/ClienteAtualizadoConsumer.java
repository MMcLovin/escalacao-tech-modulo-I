package consumers;

import services.ClienteService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ClienteAtualizadoConsumer {

    private static final Logger LOGGER = Logger.getLogger(ClienteAtualizadoConsumer.class);

    @Inject
    ClienteService clienteService;

    // Escuta o canal do Kafka configurado no application.properties
    @Incoming("cliente-atualizado")
    public Uni<Void> consumir(String cpf) {
        LOGGER.infof("=== KAFKA EVENT RECEIVED === Cliente atualizado: CPF %s. Disparando eviccao de cache...", cpf);
        
        return clienteService.limparCacheCliente(cpf);
    }
}
