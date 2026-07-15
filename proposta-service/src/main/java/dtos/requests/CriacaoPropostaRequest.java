package dtos.requests;

import constants.MensagensNegociais;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.br.CPF;
import java.math.BigDecimal;

public record CriacaoPropostaRequest(
        
        @NotBlank(message = MensagensNegociais.CPF_OBRIGATORIO)
        String cpf,

        @NotNull(message = MensagensNegociais.VALOR_IMOVEL_OBRIGATORIO)
        @Positive(message = MensagensNegociais.VALOR_IMOVEL_MAIOR_QUE_ZERO)
        BigDecimal valorImovel,

        @NotNull(message = MensagensNegociais.VALOR_ENTRADA_OBRIGATORIO)
        @Positive(message = MensagensNegociais.VALOR_ENTRADA_MAIOR_QUE_ZERO)
        BigDecimal valorEntrada
) {
}
