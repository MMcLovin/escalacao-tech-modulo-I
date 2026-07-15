# ADR-002: Estratégia de Caching, Resiliência e Invalidação para Dados do Cliente

## Status
Aprovado

## Contexto
O serviço `proposta-service` (orquestrador da esteira) necessita obter dados cadastrais e financeiros (Nome, Renda e Ocupação) por CPF do cliente a partir do microsserviço `cliente-service` (banco de dados H2). 
Essas informações são consultadas com altíssima frequência para cada proposta iniciada e sofrem alterações raras no negócio. Fazer chamadas HTTP repetitivas para o `cliente-service` adiciona latência desnecessária, consome rede e sobrecarrega a API de dados.

## Decisão
Adotaremos a arquitetura de **Cache Distribuído** utilizando a extensão **Quarkus Cache** integrada ao **Redis**, sob as seguintes definições de design:

1.  **Estratégia de Cache (Cache-Aside / Lazy Loading):**
    A aplicação gerencia a carga do cache: primeiro tenta obter a informação do Redis. Caso ocorra um *Cache Miss*, a aplicação faz a requisição HTTP via REST Client para o `cliente-service`, popula a chave no Redis e retorna a informação.
2.  **Chave de Acesso:**
    A chave única do cache no Redis será o `cpf` do cliente, garantindo que o mapeamento seja direto e rápido.
3.  **Imutabilidade do Payload (Java Record):**
    O dado armazenado será representado pela classe `DadosClienteResponse` modelada como um Java **`record`**, garantindo que o payload seja imutável e livre de efeitos colaterais de mutação de estado em memória.
4.  **Tempo de Vida (TTL - Time-To-Live):**
    Definido em properties para **10 minutos** (`expire-after-write=10m`). O TTL age como nossa rede de segurança contra dados órfãos, garantindo a expiração automática de registros inativos.
5.  **Invalidação Reativa baseada em Eventos (EDA - Event-Driven Architecture):**
    Para evitar acoplamento espacial e temporal de chamadas HTTP de evicção entre os serviços, implementamos a invalidação reativa orientada a eventos. 
    Quando o `cliente-service` altera a renda ou a ocupação de um cliente via `PUT /clientes/{cpf}`, ele persiste a alteração e publica o CPF afetado no canal Kafka `cliente-atualizado`. O `proposta-service` consome esse evento de forma assíncrona (`@Incoming`) e aciona a evicção do cache de forma puramente reativa e não bloqueante (`Uni<Void>` com `@CacheInvalidate`), mantendo o cache e o banco de dados H2 sincronizados quase em tempo real sem qualquer overhead HTTP.
6.  **Não Cacheamento de Ausência (Bypass de HTTP 404):**
    Aproveitaremos o comportamento nativo do Quarkus Cache (que não grava em cache métodos que lançam exceções). Para isso, desenhamos a estrutura do código de forma que o método anotado com `@CacheResult` (`buscarNoCache`) propague livremente o erro `404 Not Found` lançado pelo REST Client. Tratamos erros e fluxos de recuperação apenas no método de serviço chamador (`obterDadosCliente`). Isso impede o cacheamento de ausências temporárias de cadastro no Redis.
7.  **Fallback de Conectividade:**
    O cache é tratado como descartável e o banco de dados/API externa é a única fonte da verdade. Se o Redis estiver offline, a camada de serviço do `proposta-service` capturará o erro de conexão através de operadores reativos (`onFailure().recoverWithUni(...)` do Mutiny) e redirecionará a chamada HTTP diretamente para o `cliente-service`, sem interromper o serviço.

## Consequências
*   **Positivas (Prós):**
    *   **Latência Mínima:** Tempo de resposta de sub-milissegundos em consultas repetidas do mesmo CPF.
    *   **Independência de Escala:** Como o cache é distribuído (Redis) e externo ao processo, o `proposta-service` pode ser escalado horizontalmente (múltiplas réplicas) sem gerar visões divergentes.
    *   **Desacoplamento por Eventos (EDA):** O `cliente-service` não precisa conhecer a infraestrutura ou URLs do orquestrador `proposta-service` para limpar o cache; ele apenas anuncia a alteração.
    *   **Graceful Degradation:** Resiliência caso a infraestrutura do Redis falhe.
    *   **Consistência Rápida e Resiliente:** O uso do Kafka para evicção resolve a consistência eventual e sobrevive mesmo que o orquestrador esteja temporariamente fora do ar na hora da edição.
*   **Negativas (Contras):**
    *   Complexidade adicional de infraestrutura (Zookeeper + Kafka) e no código assíncrono para garantir que as threads do Vert.x Event Loop não sejam bloqueadas nas chamadas de evicção do Redis.

## Alternativas Consideradas

### 1. Invalidação via Chamada HTTP Síncrona (Descartada)
Cogitou-se expor um endpoint HTTP no `proposta-service` (ex: `POST /configuracoes/cache/clientes/{cpf}/evict`) anotado com `@CacheInvalidate`, o qual seria chamado de forma síncrona pelo `cliente-service` toda vez que um cadastro fosse atualizado.
*   **Motivo do descarte:** Essa abordagem geraria acoplamento espacial (o `cliente-service` precisaria conhecer as URLs do `proposta-service` e de quaisquer outros futuros consumidores de dados do cliente) e acoplamento temporal (o fluxo de atualização de cadastro falharia ou ficaria mais lento se o `proposta-service` estivesse instável). O descarte foi embasado nos conceitos de "Endpoints Espertos e Canos Burros" da Aula 3.

