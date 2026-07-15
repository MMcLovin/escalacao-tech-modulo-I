# ADR-003 — Idempotência do Consumidor Kafka

**Status:** Aceita  
**Data:** 2025-07-10  
**Autores:** Equipe Escalação Tech

## Contexto

Com a garantia de entrega `at-least-once` do Kafka (ADR-002), mensagens podem ser reentregues ao consumidor em cenários como rebalance de consumer group, restart da aplicação ou falha de rede antes do commit do offset. O `ClienteAtualizadoConsumer` no `proposta-service` precisa ser idempotente para evitar efeitos colaterais duplicados.

## Alternativas Consideradas

### 1. Nenhum controle de idempotência (confiar na natureza da operação)
- **Prós:** Zero complexidade adicional. A invalidação de cache é, por si só, uma operação idempotente — invalidar duas vezes o mesmo CPF não causa dano.
- **Contras:** Não demonstra o padrão exigido. Operações futuras no consumer (ex: notificações, atualização de status) podem não ser naturalmente idempotentes.

### 2. Tabela de idempotência em banco de dados
- **Prós:** Persistente entre restarts; funciona em ambiente distribuído com múltiplas instâncias.
- **Contras:** Requer base de dados no `proposta-service` (que hoje não tem); overhead de I/O em cada mensagem; complexidade desproporcional para o caso de uso atual.

### 3. ConcurrentHashMap em memória com chave topic:partition:offset (escolhida)
- **Prós:** Simples, rápido (O(1) para lookup), sem dependência externa; suficiente para o caso de uso de invalidação de cache; detecta duplicatas dentro do mesmo ciclo de vida do pod.
- **Contras:** Perde o registro ao reiniciar a aplicação; não funciona em cenários multi-instância sem coordenação externa.

### 4. Kafka Streams com exactly-once semantics (EOS)
- **Prós:** Garantia de processamento exatamente uma vez.
- **Contras:** Overhead significativo; requer configuração transacional no producer e consumer; complexidade desproporcional para invalidação de cache.

## Decisão

Adotamos **ConcurrentHashMap em memória** com chave composta `topic:partition:offset` extraída dos metadados Kafka (`IncomingKafkaRecordMetadata`).

**Fluxo:**
1. Mensagem chega no `ClienteAtualizadoConsumer`.
2. Extrai `topic:partition:offset` dos metadados Kafka.
3. Verifica se a chave já existe no mapa de mensagens processadas.
4. Se **já existe**: loga duplicata, faz `ack()` e ignora.
5. Se **não existe**: processa (invalida cache), registra a chave, faz `ack()`.

**Classe:** `consumers.ClienteAtualizadoConsumer`

## Trade-offs

| Aspecto | Benefício obtido | Custo aceito |
|---|---|---|
| Simplicidade | Implementação em poucas linhas, sem dependência externa | Registros perdidos ao reiniciar (aceitável pois offsets Kafka avançam) |
| Performance | Lookup O(1) em memória | Consumo de memória cresce com volume (mitigado: chaves são strings curtas) |
| Escopo | Suficiente para invalidação de cache | Insuficiente para side-effects críticos (ex: cobranças) — nesses casos, usar tabela em banco |

## Consequências

- Se a aplicação reiniciar e o Kafka reentegar mensagens já processadas (antes do último commit), elas serão reprocessadas. Isso é aceitável porque invalidar cache é idempotente por natureza.
- Para operações com side-effects não-idempotentes no futuro, a estratégia deve ser migrada para tabela em banco de dados.
- O mapa expõe `getMensagensProcessadas()` para permitir testes unitários de verificação de duplicatas.
