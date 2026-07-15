package dtos.responses;

import java.math.BigDecimal;

public record DadosClienteResponse(String cpf, String nome, String ocupacao, BigDecimal renda) {
}
