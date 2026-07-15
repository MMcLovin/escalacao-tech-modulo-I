package repositories;

import entities.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

@ApplicationScoped
public class ClienteRepository implements PanacheRepositoryBase<Cliente, String> {

    @Transactional
    public Cliente atualizarDados(String cpf, BigDecimal renda, String ocupacao) {
        Cliente cliente = findById(cpf);
        if (cliente != null) {
            cliente.setRenda(renda); // Renda é obrigatória, atualiza sempre
            
            // Só atualiza a ocupação se ela não for nula e nem vazia
            if (ocupacao != null && !ocupacao.isBlank()) {
                cliente.setOcupacao(ocupacao);
            }
            
            persist(cliente);
        }
        return cliente;
    }
}
