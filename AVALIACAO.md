# Auto-avaliação — Jornada de Financiamento Habitacional (Quarkus)

*   **Grupo:** <integrantes e papéis>
*   **Tema/domínio:** Fluxo de contratação de financiamento habitacional assíncrono e reativo com Quarkus, Kafka e Redis.
*   **Perfil de execução:** A (Docker com Postgres/Kafka/Redis) | B (JVM Puro / Fallback)

---

## Evidências por Critério

### 1. Decomposição de Domínio — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:** 
    *   Módulos segregados no Maven: [proposta-service](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/proposta-service), [analise-documental-service](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/analise-documental-service) e [avaliacao-imovel-service](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/avaliacao-imovel-service).
    *   Bancos de dados segregados criados via [init-db.sql](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/init-db.sql).
    *   ADR sobre a decomposição de domínios em: `docs/adr/ADR-001-decomposicao-dominios.md`

### 2. Comunicação Assíncrona — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Envio do evento reativo em: `proposta-service` (ver emitter no Resource/Controller).
    *   Consumo do evento reativo com SmallRye Reactive Messaging em: `analise-documental-service` e `avaliacao-imovel-service`.
    *   Declaração da garantia de entrega (at-least-once) descrita em: `docs/arquitetura.md`

### 3. Idempotência e Consistência — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Implementação do consumidor idempotente em: <caminho da classe que faz a validação do ID do evento no banco ou cache>
    *   Tabela ou estrutura utilizada: <nome da tabela de controle, ex: `processed_events` ou logs no Redis>

### 4. Cache — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Configuração do cache no Redis em: `proposta-service` ([application.properties](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/proposta-service/src/main/resources/application.properties)).
    *   Endpoint de consulta de taxas com cache-aside `@CacheResult`: <classe Java/caminho>
    *   Endpoint de invalidação explícita com `@CacheInvalidate`: <classe Java/caminho>

### 5. Resiliência — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Tratamento de erros no Kafka e políticas de Retry/DLQ configuradas em: <caminho do arquivo ou config no application.properties>

### 6. Testabilidade — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Teste de contrato executável usando Pact em: <caminho do teste do consumidor e teste do provedor>
    *   Garantia de que os testes rodam offline/sem docker (Perfil B): <criação de mocks ou testes JVM puros>

### 7. Decisões Arquiteturais (ADRs) — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Documentos localizados na pasta `docs/adr/`.

### 8. Uso Crítico de IA — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Reflexão sobre o uso de IA no design de arquitetura e código: <descrição do que foi validado manualmente e refinado pelo grupo>

### 9. Execução Comprovada — nível auto-atribuído: [ ] (0 a 3)
*   **Evidência:**
    *   Instruções detalhadas descritas na seção "Como rodar" do [README.md](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/README.md).
