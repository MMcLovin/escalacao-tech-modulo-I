# Jornada de Financiamento Habitacional

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

