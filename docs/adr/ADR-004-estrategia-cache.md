# ADR-004 — Estratégia de Cache

**Status:** Aceita  
**Data:** 2025-07-10  
**Autores:** Equipe Escalação Tech

## Contexto

O `proposta-service` consulta frequentemente o `cliente-service` via REST para obter dados cadastrais (CPF, nome, renda, ocupação) durante a criação de propostas. Esses dados mudam com pouca frequência, mas são lidos intensivamente. Sem cache, cada proposta geraria uma chamada HTTP ao `cliente-service`, aumentando latência e carga no serviço remoto.

## Alternativas Consideradas

### 1. Sem cache (chamada direta sempre)
- **Prós:** Dados sempre frescos; zero complexidade.
- **Contras:** Latência alta em cada requisição; pressão desnecessária no `cliente-service`; sem resiliência a indisponibilidade temporária do serviço remoto.

### 2. Cache local em memória (Caffeine/ConcurrentHashMap)
- **Prós:** Muito rápido; sem dependência externa; simples.
- **Contras:** Não compartilhado entre instâncias; perde tudo ao reiniciar; difícil de invalidar de forma coordenada.

### 3. Cache distribuído com Redis via Quarkus Cache (escolhida)
- **Prós:** Compartilhado entre instâncias; persistente entre restarts; integração nativa com Quarkus (`@CacheResult`, `@CacheInvalidate`); suporte a TTL configurável.
- **Contras:** Dependência externa (Redis); latência de rede ao cache; necessidade de fallback se Redis ficar indisponível.

## Decisão

Adotamos **Redis como cache distribuído** com a abstração **Quarkus Cache** (`quarkus-redis-cache`).

**Estratégia: Cache-Aside (Lazy Loading) com invalidação por evento.**

### Leitura (cache-aside)
1. `ClienteService.buscarNoCache(cpf)` é anotado com `@CacheResult(cacheName = "dados-cliente-cache")`.
2. **Cache hit:** retorna diretamente do Redis sem chamar o `cliente-service`.
3. **Cache miss:** chama `cliente-service` via REST, armazena o resultado no Redis, retorna ao chamador.

### TTL (Time-To-Live)
- Configurado em `application.properties`: `quarkus.cache.redis."dados-cliente-cache".expire-after-write=10m`
- **Justificativa:** 10 minutos é um equilíbrio entre frescor dos dados e redução de chamadas. Dados cadastrais raramente mudam mais de uma vez por dia.

### Invalidação explícita por evento Kafka
- Quando o `cliente-service` atualiza um cliente, publica o CPF no tópico `cliente-atualizado`.
- O `ClienteAtualizadoConsumer` no `proposta-service` consome o evento e chama `ClienteService.limparCacheCliente(cpf)` anotado com `@CacheInvalidate`.
- Isso garante que dados stale sejam removidos antes do TTL expirar.

### Fallback quando Redis está indisponível
- Se o Redis estiver offline, `buscarNoCache()` falha e o fallback em `obterDadosCliente()` chama diretamente o `cliente-service` via REST (`.onFailure().recoverWithUni(...)`).
- O sistema continua funcional, apenas sem o benefício do cache.

## Trade-offs

| Aspecto | Benefício obtido | Custo aceito |
|---|---|---|
| TTL de 10 minutos | Reduz ~95% das chamadas ao cliente-service | Dados podem ficar stale por até 10 min (mitigado por invalidação via evento) |
| Invalidação por evento | Cache sempre fresco após atualizações | Dependência do Kafka; consistência eventual |
| Fallback sem cache | Sistema não para se Redis cair | Latência maior e mais carga no cliente-service durante falha do Redis |
| Cache-aside | Simples de implementar; só cacheia dados acessados | Primeiro acesso sempre lento (cold start) |

## Consequências

- A invalidação depende de eventos Kafka — se o evento for perdido, o cache fica stale até o TTL expirar (máximo 10 min).
- O consumer de invalidação deve ser idempotente — ver ADR-003.
- Para testes sem Redis, o Quarkus Cache pode ser configurado com provider `caffeine` (in-memory) via perfil de teste.
