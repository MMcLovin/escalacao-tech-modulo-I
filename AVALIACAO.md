# Auto-avaliação — Sistema de Avaliacao Habitacional
Grupo: Antonio David Lima - Alexandre Machado - Gabriel Fernando
Tema/domínio: Microsserviços para avaliação habitacional com decomposição por domínio.
Cadastro de clientes e propostas ficam separados, com REST, Kafka e Redis.
Perfil de execução: B  ·  Fallbacks usados: Docker Compose para Redis/Kafka/ZooKeeper; fallback HTTP direto ao `cliente-service` quando o Redis falha

## Evidências por critério
1. Decomposição de domínio — nível auto-atribuído: Atendido
   Evidência: [pom.xml](pom.xml), [cliente-service/src/main/resources/application.properties](cliente-service/src/main/resources/application.properties), [proposta-service/src/main/resources/application.properties](proposta-service/src/main/resources/application.properties), [docs/adr/ADR-001-decomposicao-dominios.md](docs/adr/ADR-001-decomposicao-dominios.md), [README.md](README.md)
2. Comunicação assíncrona — Atendido
   Evidência: [cliente-service/src/main/java/resources/ClienteResource.java](cliente-service/src/main/java/resources/ClienteResource.java), [proposta-service/src/main/java/consumers/ClienteAtualizadoConsumer.java](proposta-service/src/main/java/consumers/ClienteAtualizadoConsumer.java), [proposta-service/src/main/resources/application.properties](proposta-service/src/main/resources/application.properties), [docs/adr/ADR-006-comunicacao-assincrona.md](docs/adr/ADR-006-comunicacao-assincrona.md)
3. Idempotência e consistência — Atendido
   Evidência: [proposta-service/src/main/java/consumers/ClienteAtualizadoConsumer.java](proposta-service/src/main/java/consumers/ClienteAtualizadoConsumer.java), [docs/adr/ADR-003-idempotencia.md](docs/adr/ADR-003-idempotencia.md), [docs/adr/ADR-006-comunicacao-assincrona.md](docs/adr/ADR-006-comunicacao-assincrona.md)
4. Cache — Atendido
   Evidência: [proposta-service/src/main/java/services/ClienteService.java](proposta-service/src/main/java/services/ClienteService.java), [proposta-service/src/main/resources/application.properties](proposta-service/src/main/resources/application.properties), [docs/adr/ADR-004-estrategia-cache.md](docs/adr/ADR-004-estrategia-cache.md), [docs/adr/ADR-002-cache-redis.md](docs/adr/ADR-002-cache-redis.md)
5. Resiliência — Atendido
   Evidência: [proposta-service/src/main/java/services/ClienteService.java](proposta-service/src/main/java/services/ClienteService.java), [proposta-service/src/main/resources/application.properties](proposta-service/src/main/resources/application.properties), [docs/adr/ADR-005-resiliencia.md](docs/adr/ADR-005-resiliencia.md)
6. Testabilidade — Atendido
   Evidência: [proposta-service/src/test/java/contracts/ClienteRestClientPactTest.java](proposta-service/src/test/java/contracts/ClienteRestClientPactTest.java), [proposta-service/target/pacts/proposta-service-cliente-service.json](proposta-service/target/pacts/proposta-service-cliente-service.json)
7. Decisões arquiteturais — Atendido
   Evidência: [docs/adr/ADR-001-decomposicao-dominios.md](docs/adr/ADR-001-decomposicao-dominios.md), [docs/adr/ADR-002-cache-redis.md](docs/adr/ADR-002-cache-redis.md), [docs/adr/ADR-003-idempotencia.md](docs/adr/ADR-003-idempotencia.md), [docs/adr/ADR-004-estrategia-cache.md](docs/adr/ADR-004-estrategia-cache.md), [docs/adr/ADR-005-resiliencia.md](docs/adr/ADR-005-resiliencia.md), [docs/adr/ADR-006-comunicacao-assincrona.md](docs/adr/ADR-006-comunicacao-assincrona.md)
8. Uso crítico de IA — Atendido
   Como usamos IA e o que validamos manualmente: usei IA para estruturar e revisar a documentação, localizar evidências e montar o contrato executável. Validei manualmente os arquivos do repositório e executei o teste Pact com sucesso para confirmar a integração.
9. Execução — Atendido
   Como rodar: `docker compose up -d`; `mvn -pl cliente-service quarkus:dev`; `mvn -pl proposta-service quarkus:dev`; `mvn -pl proposta-service -Dtest=ClienteRestClientPactTest test`. Perfil declarado: `%dev`.

## Opcionais entregues
Nenhum adicional.
