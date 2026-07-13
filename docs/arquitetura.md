# Visão de Arquitetura

Este documento detalha o fluxo de eventos e decisões de design da **Jornada de Financiamento Habitacional**.

## Diagrama de Contextos e Fluxo de Eventos

```mermaid
sequenceDiagram
    actor Cliente
    participant Proposta as proposta-service (Port: 8081)
    participant Redis as Redis Cache
    participant Kafka as Apache Kafka (Topic: proposta-criada)
    participant Analise as analise-documental-service (Port: 8082)
    participant Avaliacao as avaliacao-imovel-service (Port: 8083)

    %% Cenário 1: Consulta de Taxas
    Cliente->>Proposta: GET /configuracoes/taxas
    alt Cache Hit
        Proposta-->>Cliente: Retorna taxas do Redis (Rápido)
    else Cache Miss
        Proposta->>Proposta: Consulta taxas no Banco de Dados
        Proposta->>Redis: Salva taxas no Cache
        Proposta-->>Cliente: Retorna taxas
    end

    %% Cenário 2: Criação da Proposta
    Cliente->>Proposta: POST /propostas (Criar Proposta)
    Proposta->>Proposta: Salva proposta no Postgres (proposta_db)
    Proposta->>Kafka: Publica evento "PropostaCriadaEvent"
    Proposta-->>Cliente: Retorna HTTP 201 (Created)
    Note over Cliente, Proposta: Processo agora corre em segundo plano (Assíncrono)

    %% Processamento Assíncrono
    par Análise Documental
        Kafka->>Analise: Recebe PropostaCriadaEvent
        Analise->>Analise: Executa verificação de Idempotência
        Note right of Analise: Se evento já processado, descarta.
        Analise->>Analise: Simula análise dos documentos
        Analise->>Analise: Persiste resultado na base (analise_db)
    and Avaliação do Imóvel
        Kafka->>Avaliacao: Recebe PropostaCriadaEvent
        Avaliacao->>Avaliacao: Executa verificação de Idempotência
        Avaliacao->>Avaliacao: Avalia valor do imóvel e gera laudo
        Avaliacao->>Avaliacao: Persiste resultado na base (avaliacao_db)
    end
```

## Tecnologias e Trade-offs

1.  **Quarkus Reativo (Mutiny):** Escolhido para maximizar a taxa de transferência e a eficiência de recursos nas requisições I/O intensivas (banco de dados, Redis, Kafka).
2.  **PostgreSQL Segregado:** Cada serviço possui sua própria base de dados (`proposta_db`, `analise_db`, `avaliacao_db`). Isso impede acoplamento por dados e permite que cada microsserviço evolua seu esquema de forma independente.
3.  **Apache Kafka:** Broker de mensageria assíncrona robusto que permite que a criação da proposta seja instantânea para o cliente final, desacoplando o fluxo de análise e avaliação.
4.  **Redis (Cache-Aside):** Utilizado para tabelas altamente lidas com alterações infrequentes (como taxas de juros e modalidades), diminuindo significativamente a latência e o consumo do banco de dados relacional.
