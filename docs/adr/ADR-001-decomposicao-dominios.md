# ADR-001 — Decomposição de Domínios

**Status:** Aceita  
**Data:** 2025-07-10  
**Autores:** Equipe Escalação Tech

## Contexto

O sistema de avaliação habitacional precisa gerenciar dois domínios distintos: **dados cadastrais de clientes** (CPF, nome, renda, ocupação) e **propostas/avaliação de contratos** (criação de proposta, análise de crédito, resultado da avaliação). Esses domínios possuem ciclos de vida, regras de negócio e frequências de acesso diferentes.

Precisávamos decidir se manteríamos tudo em um monolito ou se separaríamos em serviços independentes.

## Alternativas Consideradas

### 1. Monolito Modular
- **Prós:** Simplicidade operacional, sem latência de rede entre módulos, transações ACID locais.
- **Contras:** Acoplamento crescente entre domínios; deploy único obriga redeployar tudo; escalabilidade uniforme (não dá para escalar apenas a parte mais demandada).

### 2. Dois Microsserviços com Bases Segregadas (escolhida)
- **Prós:** Bounded contexts claros; cada serviço pode evoluir, escalar e ser deployed independentemente; bases de dados segregadas eliminam acoplamento por dados.
- **Contras:** Complexidade operacional (dois processos, comunicação via rede); consistência eventual entre serviços; necessidade de mecanismos de resiliência.

### 3. Três ou mais microsserviços (ex: separar avaliação de contrato)
- **Prós:** Granularidade máxima.
- **Contras:** Over-engineering para o escopo atual; complexidade desproporcional ao benefício.

## Decisão

Adotamos **dois microsserviços** com bases de dados segregadas:

- **`cliente-service`** — gerencia dados cadastrais de clientes. Base: H2 em memória (`clientes_db`). Porta 8084.
- **`proposta-service`** — orquestra criação de propostas, consulta dados de cliente via REST + cache, e envia propostas para avaliação. Porta 8080.

Cada serviço tem seu próprio módulo Maven, configuração independente e base de dados isolada. A comunicação entre eles é feita via REST (síncrono) e Kafka (assíncrono).

## Trade-offs

| Aspecto | Benefício obtido | Custo aceito |
|---|---|---|
| Independência de deploy | Cada serviço evolui sozinho | Necessidade de versionamento de APIs |
| Bases segregadas | Zero acoplamento por dados | Consistência eventual (resolvida via eventos Kafka) |
| Escalabilidade seletiva | Escalar apenas o serviço mais demandado | Dois processos JVM para gerenciar |
| Simplicidade do domínio | Bounded contexts limpos | Latência de rede nas chamadas entre serviços |

## Consequências

- Comunicação entre serviços requer mecanismos de resiliência (retry, circuit breaker, timeout) — ver ADR-005.
- Invalidação de cache do `proposta-service` depende de eventos Kafka publicados pelo `cliente-service` — ver ADR-004.
- Consistência eventual é aceitável: dados de cliente são consultados em leitura e raramente mudam durante uma avaliação.
