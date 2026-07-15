# ADR-005 — Resiliência

**Status:** Aceita  
**Data:** 2025-07-10  
**Autores:** Equipe Escalação Tech

## Contexto

O `proposta-service` depende de chamadas HTTP ao `cliente-service` e de conexão ao Redis para cache. Em um ambiente distribuído, falhas transitórias (rede lenta, serviço temporariamente indisponível) e permanentes (serviço fora do ar) são inevitáveis. Sem mecanismos de resiliência, uma falha em cascata pode derrubar todo o fluxo de criação de propostas.

Também precisamos lidar com mensagens Kafka que não podem ser processadas, evitando que bloqueiem o consumidor indefinidamente.

## Alternativas Consideradas

### 1. Nenhum mecanismo de resiliência
- **Prós:** Simplicidade total.
- **Contras:** Qualquer falha transitória vira erro para o usuário; sem recuperação automática; risco de cascading failure.

### 2. Retry simples (fixo, sem backoff)
- **Prós:** Recupera de falhas transitórias curtas.
- **Contras:** Pode sobrecarregar o serviço já degradado (thundering herd); sem proteção contra falhas longas.

### 3. MicroProfile Fault Tolerance completo + DLQ no Kafka (escolhida)
- **Prós:** Cobertura completa: retry com backoff para falhas transitórias, timeout para evitar bloqueio, circuit breaker para proteger contra falhas longas, fallback para degradação graciosa, DLQ para mensagens não processáveis.
- **Contras:** Mais configuração; comportamento pode ser difícil de debugar; precisa ajustar thresholds com dados reais.

## Decisão

Adotamos uma **estratégia de resiliência em camadas** usando MicroProfile Fault Tolerance (via `quarkus-smallrye-fault-tolerance`) para chamadas HTTP e Dead Letter Queue para mensageria Kafka.

### Camada 1 — Timeout
- **Anotação:** `@Timeout(value = 5000)` no método `buscarNoCache()`
- **Justificativa:** Se o `cliente-service` não responder em 5 segundos, a chamada é abortada. Evita que threads fiquem presas esperando um serviço degradado.

### Camada 2 — Retry com Backoff
- **Anotação:** `@Retry(maxRetries = 3, delay = 500, jitter = 200)`
- **Comportamento:** Até 3 tentativas, com delay base de 500ms e jitter de ±200ms para evitar thundering herd.
- **Justificativa:** Falhas transitórias (timeout de rede, 503 temporário) são recuperáveis com retry. O jitter distribui as tentativas no tempo.

### Camada 3 — Circuit Breaker
- **Anotação:** `@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)`
- **Comportamento:**
  - **Fechado (normal):** Requisições passam normalmente.
  - **Aberto:** Após 4 requisições com ≥50% de falha, o circuito abre por 10 segundos. Requisições são rejeitadas imediatamente (fail-fast).
  - **Half-open:** Após 10 segundos, permite 2 requisições de teste. Se ambas tiverem sucesso, fecha o circuito.
- **Justificativa:** Protege o `cliente-service` de ser bombardeado quando está degradado. Permite recuperação gradual.

### Camada 4 — Fallback (Mutiny)
- **Implementação:** `.onFailure().recoverWithUni(...)` no método `obterDadosCliente()`
- **Comportamento:** Se todas as camadas acima falharem (incluindo circuit breaker aberto), tenta uma chamada direta ao `cliente-service` sem cache.
- **Justificativa:** Degradação graciosa — melhor retornar com latência maior do que falhar completamente.

### Camada 5 — Dead Letter Queue (Kafka)
- **Configuração:** `mp.messaging.incoming.cliente-atualizado.failure-strategy=dead-letter-queue`
- **Tópico DLQ:** `cliente-atualizado-dlq`
- **Comportamento:** Mensagens que o consumidor não consegue processar são enviadas para a DLQ em vez de bloquear o consumo do tópico principal.
- **Justificativa:** Isola mensagens problemáticas (payload corrompido, erro inesperado) para análise posterior sem impactar o fluxo normal.

### Ordem de execução das camadas (chamada HTTP)
```
Requisição → Timeout(5s) → Retry(3x, 500ms+jitter) → CircuitBreaker → Fallback
```

## Trade-offs

| Aspecto | Benefício obtido | Custo aceito |
|---|---|---|
| Retry com jitter | Recupera falhas transitórias sem thundering herd | Aumenta latência total em caso de falha (até ~2s extras) |
| Circuit breaker | Protege serviço degradado; fail-fast | Requisições legítimas são rejeitadas quando circuito está aberto |
| Timeout de 5s | Evita threads presas | Chamadas legítimas mas lentas são abortadas |
| DLQ | Mensagens problemáticas não bloqueiam o consumo | Requer monitoramento/reprocessamento manual da DLQ |
| Fallback direto | Sistema continua funcional mesmo com cache/retry falhando | Chamada sem cache aumenta carga no cliente-service |

## Consequências

- Os thresholds (5s timeout, 3 retries, 50% failure ratio) são valores iniciais que devem ser ajustados com dados de produção.
- A DLQ (`cliente-atualizado-dlq`) precisa ser monitorada — mensagens lá indicam problemas que requerem atenção.
- O fallback cria um "caminho feliz degradado": o sistema funciona, mas sem as proteções do cache. Em caso de falha prolongada do Redis E do cliente-service, o fallback também falha e o erro é retornado ao chamador.
