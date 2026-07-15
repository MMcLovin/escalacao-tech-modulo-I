package consumers;

import services.ClienteService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumidor Kafka idempotente para eventos de atualização de cliente.
 *
 * Estratégia de idempotência: armazena o offset+partition de cada mensagem já processada
 * em um ConcurrentHashMap em memória. Se a mesma mensagem for reentregue (ex: rebalance do
 * consumer group), o processamento é ignorado, evitando invalidações de cache duplicadas.
 *
 * Trade-off: em memória é suficiente aqui porque o efeito colateral é apenas invalidação
 * de cache (operação naturalmente idempotente). Para side-effects não-idempotentes,
 * seria necessário persistir as chaves em banco de dados.
 */
@ApplicationScoped
public class ClienteAtualizadoConsumer {

    private static final Logger LOGGER = Logger.getLogger(ClienteAtualizadoConsumer.class);

    @Inject
    ClienteService clienteService;

    // Registro de mensagens já processadas: chave = "topic:partition:offset"
    private final Map<String, Boolean> mensagensProcessadas = new ConcurrentHashMap<>();

    @Incoming("cliente-atualizado")
    public Uni<Void> consumir(Message<String> message) {
        String cpf = message.getPayload();

        // Monta chave de idempotência a partir dos metadados do Kafka
        String chaveIdempotencia = extrairChaveIdempotencia(message);

        if (chaveIdempotencia != null && mensagensProcessadas.containsKey(chaveIdempotencia)) {
            LOGGER.infof("=== MENSAGEM DUPLICADA IGNORADA === Chave: %s, CPF: %s", chaveIdempotencia, cpf);
            return Uni.createFrom().completionStage(message.ack());
        }

        LOGGER.infof("=== KAFKA EVENT RECEIVED === Cliente atualizado: CPF %s. Disparando eviccao de cache...", cpf);

        return clienteService.limparCacheCliente(cpf)
                .invoke(() -> {
                    if (chaveIdempotencia != null) {
                        mensagensProcessadas.put(chaveIdempotencia, Boolean.TRUE);
                        LOGGER.infof("Mensagem registrada como processada: %s", chaveIdempotencia);
                    }
                })
                .chain(() -> Uni.createFrom().completionStage(message.ack()));
    }

    /**
     * Extrai a chave de idempotência dos metadados do Kafka (topic:partition:offset).
     * Retorna null caso os metadados não estejam disponíveis.
     */
    private String extrairChaveIdempotencia(Message<String> message) {
        var metadata = message.getMetadata(io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata.class);
        if (metadata.isPresent()) {
            var kafkaMeta = metadata.get();
            return kafkaMeta.getTopic() + ":" + kafkaMeta.getPartition() + ":" + kafkaMeta.getOffset();
        }
        return null;
    }

    // Visível para testes: permite verificar o estado interno
    Map<String, Boolean> getMensagensProcessadas() {
        return mensagensProcessadas;
    }
}
