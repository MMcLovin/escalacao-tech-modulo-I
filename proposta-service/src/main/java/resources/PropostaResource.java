package resources;

import dtos.requests.CriacaoPropostaRequest;
import services.ClienteService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/propostas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PropostaResource {

    @Inject
    ClienteService clienteService;

    @POST
    public Uni<Response> criar(@Valid CriacaoPropostaRequest request) {

        // Por enquanto, tô retornando os dados do cliente, obtidos do serviço de clientes, pra testar a integração entre os serviços. TODO: implementar a "criação" da proposta/contrato e envio para avaliação.
        return clienteService.obterDadosCliente(request.cpf())
                .map(dadosCliente -> {
                    return Response.status(Response.Status.ACCEPTED).entity(dadosCliente).build();
                });
    }
}
