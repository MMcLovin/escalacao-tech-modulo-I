package resources;

import dtos.requests.AtualizarClienteRequest;
import dtos.responses.DadosClienteResponse;
import entities.Cliente;
import org.jboss.logging.Logger;
import repositories.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import java.math.BigDecimal;

@ApplicationScoped
@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    @Channel("cliente-atualizado")
    Emitter<String> clientUpdatedEmitter;

    private static final Logger LOGGER = Logger.getLogger(ClienteResource.class);

    @GET
    @Path("/{cpf}")
    public Response obterPorCpf(@PathParam("cpf") String cpf) {
        Cliente cliente = clienteRepository.findById(cpf);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        DadosClienteResponse response = new DadosClienteResponse(
            cliente.cpf, 
            cliente.nome, 
            cliente.ocupacao, 
            cliente.renda
        );

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{cpf}")
    public Response atualizarPorCpf(@PathParam("cpf") String cpf, @Valid AtualizarClienteRequest request) {
        Cliente cliente = clienteRepository.atualizarDados(cpf, request.renda(), request.ocupacao());

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Publica no Kafka o CPF do cliente atualizado
        clientUpdatedEmitter.send(cliente.cpf);
        LOGGER.info("=== KAFKA EVENT PUBLISHED === Cliente atualizado: CPF %s.".formatted(cliente.cpf));

        DadosClienteResponse response = new DadosClienteResponse(
            cliente.cpf, 
            cliente.nome, 
            cliente.ocupacao, 
            cliente.renda
        );

        return Response.ok(response).build();
    }
}
