# ADR-001: Decomposição de Domínio e Segregação de Bases de Dados

## Status
Aprovado

## Contexto
O processo de financiamento habitacional envolve etapas de natureza e tempos de execução distintos (ex: criação instantânea de propostas vs. análise de documentos complexos que pode levar dias). Um design monolítico ou com base de dados compartilhada criaria acoplamento excessivo e impediria o escalonamento independente de cada etapa.

## Decisão
Decidimos dividir a solução em três microsserviços distintos baseados em seus respectivos Bounded Contexts:
1.  **proposta-service:** Gerencia dados cadastrais e solicitação inicial.
2.  **analise-documental-service:** Valida e analisa os arquivos de comprovantes e documentos.
3.  **avaliacao-imovel-service:** Registra e emite laudos sobre a garantia física (imóvel).

Cada serviço possuirá seu próprio esquema e instância lógica de banco de dados PostgreSQL (`proposta_db`, `analise_db` e `avaliacao_db` no container do Docker), garantindo isolamento total de dados. A sincronização de estado ocorrerá via eventos assíncronos.

## Consequências
*   **Positivas (Prós):**
    *   Independência de deploy e desenvolvimento paralelo por equipe/desenvolvedor.
    *   Garantia de que nenhum serviço acessa diretamente tabelas de outros (sem acoplamento oculto).
    *   Escalonamento de recursos isolado.
*   **Negativas (Contras):**
    *   Gerenciamento de transações distribuídas (consistência eventual).
    *   Complexidade operacional para gerenciar múltiplas conexões de banco de dados localmente.
