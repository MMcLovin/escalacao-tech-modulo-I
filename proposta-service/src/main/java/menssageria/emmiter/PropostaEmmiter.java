package menssageria.emmiter;

import dtos.PropostaDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/mensagens")
@ApplicationScoped
public class PropostaEmmiter {

    // Injeta um canal de transmissão de dados lógico chamado "envio-poc"
    @Inject
    @Channel("propostas-enviadas")
    Emitter<PropostaDTO> emitter;


    public void enviarMensagem(PropostaDTO payload) {
        // O Emitter abstrai toda a criação de chaves e instâncias de ProducerRecord
        emitter.send(payload);
    }
}