package dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AtualizarClienteRequest(
        @NotNull(message = "O valor da renda é obrigatório.")
        @Positive(message = "O valor da renda deve ser maior que zero.")
        BigDecimal renda,
        String ocupacao
) {}
