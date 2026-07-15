package clients;

import dtos.responses.DadosClienteResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "cliente-api")
@Path("/clientes")
public interface ClienteRestClient {
    @GET
    @Path("/{cpf}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<DadosClienteResponse> obterDadosCliente(@PathParam("cpf" ) String cpf);
}
