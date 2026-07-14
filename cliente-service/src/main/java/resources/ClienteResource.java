package resources;

import dtos.responses.DadosClienteResponse;
import entities.Cliente;
import jakarta.enterprise.context.ApplicationScoped;
import repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class ClienteResource {

    private final ClienteRepository clienteRepository;

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
}
