package ada.dto;

import java.math.BigDecimal;

public record PropostaDTO(String cpf, String nome, String ocupacao, BigDecimal renda,
                          BigDecimal valorImovel, BigDecimal valorEntrada) {
}
