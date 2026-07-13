# Jornada de Financiamento Habitacional

(Rascunho)

## Domínios

### Proposta Service
Responsável por:

- Criação da proposta
- Dados do cliente
- Valor do imóvel

### Análise Documental Service
Responsável por:

- Documentos enviados
- Validação documental
- Pendências

### Avaliação Imóvel Service
Responsável por:

- Avaliação do imóvel
- Laudo

## Por que Kafka faz sentido?

Quando o cliente envia uma proposta:

1. Proposta criada
2. Evento publicado
3. Análise documental inicia
4. Avaliação do imóvel inicia

O usuário não precisa esperar tudo acontecer. É exatamente o padrão utilizado em processos de crédito.

## Por que cache faz sentido?

Tabela de:

- Taxas de juros
- Modalidades
- Limites

Motivação:

- Milhares de consultas por dia
- Poucas alterações

Conclusão: Redis é extremamente justificável.

---

## 🛠️ Estrutura do Projeto

O repositório está estruturado como um projeto Maven multi-módulo contendo os microsserviços reativos com Quarkus:

```text
escalacao-tech-modulo-I/
├── pom.xml                     # Parent POM configurando o Quarkus BOM
├── docker-compose.yml          # Infraestrutura local (Postgres, Kafka, Redis, Zookeeper)
├── init-db.sql                 # Script SQL para criar as bases segregadas
├── ROADMAP.md                  # Roteiro interativo de tarefas e divisão do grupo
├── AVALIACAO.md                # Template de entrega e auto-avaliação do projeto
├── shared-contracts/           # DTOs de eventos comuns aos serviços
├── proposta-service/           # Serviço de Propostas (Porta: 8081)
├── analise-documental-service/ # Serviço de Análise Documental (Porta: 8082)
├── avaliacao-imovel-service/   # Serviço de Avaliação do Imóvel (Porta: 8083)
└── docs/
    ├── arquitetura.md          # Diagrama de sequência de eventos e detalhes de design
    └── adr/                    # Pasta com os registros de decisões arquiteturais
```

---

## 🚀 Como Rodar a Infraestrutura

1. **Requisitos:** Certifique-se de ter o Docker e Docker Compose instalados na máquina.
2. **Subir Serviços (Kafka, Postgres, Redis):**
   ```bash
   docker-compose up -d
   ```
   *Isso criará os bancos segregados (`proposta_db`, `analise_db`, `avaliacao_db`) e iniciará os brokers de mensageria e cache.*
3. **Rodar em Modo Dev:**
   Entre em qualquer diretório de serviço (ex: `/proposta-service`) e execute:
   ```bash
   mvn quarkus:dev
   ```

Para acompanhar a delegação de tarefas de cada membro do grupo, consulte o arquivo [ROADMAP.md](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/ROADMAP.md).
Para conferir a entrega dos critérios do projeto, consulte o arquivo [AVALIACAO.md](file:///C:/Users/Gabriel/Documents/GitHub/Caixa/escalacao-tech-modulo-I/AVALIACAO.md).


