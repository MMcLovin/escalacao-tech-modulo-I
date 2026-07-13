package br.com.caixa.habitacional.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PropostaCriadaEvent {
    
    private UUID id;
    private String clienteNome;
    private String clienteCpf;
    private BigDecimal valorImovel;

    public PropostaCriadaEvent() {
    }

    public PropostaCriadaEvent(UUID id, String clienteNome, String clienteCpf, BigDecimal valorImovel) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.clienteCpf = clienteCpf;
        this.valorImovel = valorImovel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }

    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public BigDecimal getValorImovel() {
        return valorImovel;
    }

    public void setValorImovel(BigDecimal valorImovel) {
        this.valorImovel = valorImovel;
    }
}
