package ada.messaging.consumer;

import ada.dto.PropostaDTO;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PropostaReceiver {

    private static final Logger log = LoggerFactory.getLogger(PropostaReceiver.class);

    // Ouve o canal lógico configurado no application.properties
    @Incoming("propostas-recebidas")
    public void processar(PropostaDTO proposta) {
        log.info("Proposta recebida via Kafka. ID: {}, Valor: {}",
                proposta.cpf(), proposta.valorEntrada());

        // Implementação do domínio lógico
    }
}