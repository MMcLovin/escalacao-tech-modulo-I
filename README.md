# Sistema de Avaliacao Habitacional

## Tema

Arquitetura de microsservicos para avaliacao habitacional, com separacao de dominios, comunicacao assíncrona e cache distribuído.

## Problema

O sistema precisa atender dois dominios com ciclos de vida diferentes: cadastro de clientes e criacao/avaliacao de propostas. Esses dominios nao devem compartilhar a mesma base de dados nem o mesmo ciclo de deploy, para reduzir acoplamento e permitir evolucao independente.

## Visao de Arquitetura

A soluçao foi dividida em dois servicos:

- `cliente-service`: responsavel por persistir e atualizar dados cadastrais do cliente.
- `proposta-service`: responsavel por consultar dados de cliente, aplicar cache Redis e orquestrar o fluxo de proposta.

Os servicos se comunicam por REST para leitura sob demanda e por Kafka para eventos de atualizacao. Quando um cliente e alterado, o `cliente-service` publica o evento `cliente-atualizado`; o `proposta-service` consome esse evento e invalida o cache correspondente.

Componentes principais:

- Banco segregado por servico.
- Cache Redis no `proposta-service`.
- Kafka para fluxo assíncrono.
- Teste de contrato Pact para validar a integracao entre os servicos.

## Como Rodar

### Requisitos

- Java 17
- Maven
- Docker e Docker Compose

### Infraestrutura

Suba Redis, Kafka e ZooKeeper:

```bash
docker compose up -d
```

### Aplicacoes

Em um terminal, inicie o servico de clientes:

```bash
mvn -pl cliente-service quarkus:dev
```

Em outro terminal, inicie o servico de propostas:

```bash
mvn -pl proposta-service quarkus:dev
```

### Portas

- `cliente-service`: http://localhost:8084
- `proposta-service`: http://localhost:8080

### Testes

Executar todos os testes:

```bash
mvn test
```

Executar apenas o contrato executavel:

```bash
mvn -pl proposta-service -Dtest=ClienteRestClientPactTest test
```
