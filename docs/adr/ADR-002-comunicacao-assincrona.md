# ADR-002 — Comunicação Assíncrona

**Status:** Aceita  
**Data:** 2025-07-10  
**Autores:** Equipe Escalação Tech

## Contexto

Com a decomposição em dois microsserviços (ADR-001), precisamos de um mecanismo para que o `cliente-service` notifique o `proposta-service` sobre alterações nos dados cadastrais de clientes, permitindo invalidação de cache e, futuramente, processamento de propostas enviadas para avaliação.

A comunicação síncrona (REST) já é usada para consultas sob demanda, mas **notificações de mudança de estado** precisam ser desacopladas para evitar dependência temporal entre os serviços.

## Alternativas Consideradas

### 1. Polling periódico (proposta-service consulta cliente-service)
- **Prós:** Simples de implementar; sem infraestrutura adicional.
- **Contras:** Desperdício de recursos (maioria das consultas sem mudança); latência alta (depende do intervalo de polling); não escala bem.

### 2. Webhooks (cliente-service chama endpoint do proposta-service)
- **Prós:** Notificação em tempo real; simples.
- **Contras:** Acoplamento direto (cliente-service precisa conhecer o proposta-service); sem garantia de entrega se o consumidor estiver offline; sem replay.

### 3. Mensageria com Apache Kafka via SmallRye Reactive Messaging (escolhida)
- **Prós:** Desacoplamento total (producer não conhece consumers); persistência de mensagens permite replay; suporte nativo no Quarkus via SmallRye; garantia de entrega at-least-once com offset commit; escalabilidade horizontal via partições.
- **Contras:** Infraestrutura adicional (broker Kafka); complexidade de configuração; necessidade de idempotência no consumidor (ver ADR-003).

### 4. RabbitMQ
- **Prós:** Modelo de filas mais simples; suporte a routing complexo.
- **Contras:** Menor throughput que Kafka para o volume esperado; sem log de eventos persistente; menos integração nativa com Quarkus SmallRye.

## Decisão

Adotamos **Apache Kafka** como broker de mensagens, integrado via **SmallRye Reactive Messaging** do Quarkus.

**Tópicos definidos:**
- `cliente-atualizado` — evento emitido pelo `cliente-service` quando dados de um cliente são alterados (payload: CPF). Consumido pelo `proposta-service` para invalidação de cache.
- `propostas-enviadas` — canal para envio de propostas para avaliação de contrato.

**Garantia de entrega declarada:** `at-least-once`. O offset só é comitado após o processamento bem-sucedido da mensagem. Isso implica que mensagens podem ser reentregues, exigindo idempotência no consumidor (ver ADR-003).

## Trade-offs

| Aspecto | Benefício obtido | Custo aceito |
|---|---|---|
| Desacoplamento temporal | Serviços operam independentemente | Consistência eventual (não imediata) |
| Garantia at-least-once | Nenhuma mensagem é perdida | Possibilidade de duplicatas (mitigada por idempotência) |
| Replay de eventos | Permite reprocessamento histórico | Mais espaço em disco no broker |
| Integração SmallRye | Configuração declarativa via properties | Lock-in no ecossistema Quarkus/SmallRye |

## Consequências

- Consumidores devem ser idempotentes — ver ADR-003.
- Mensagens que falham após retries são enviadas para Dead Letter Queue (`cliente-atualizado-dlq`) — ver ADR-005.
- Para perfil B (restrito/JVM), Kafka deve estar disponível localmente ou ser substituído por SmallRye in-memory connector nos testes.
