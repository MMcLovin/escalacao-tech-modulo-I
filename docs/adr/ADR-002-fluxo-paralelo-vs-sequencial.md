# ADR-002: Fluxo de Processamento — Paralelo vs. Sequencial

## Status
Proposto (Em Discussão pelo Grupo)

## Contexto
Na esteira de financiamento habitacional, a concessão de crédito depende de duas grandes análises:
1.  **Análise Documental e de Crédito:** Validação de certidões, comprovação de renda e checagem de restrições cadastrais (foco na pessoa).
2.  **Avaliação do Imóvel:** Vistoria técnica por um engenheiro credenciado para atestar o valor de mercado e a integridade estrutural da garantia (foco no bem).

Precisamos decidir se essas etapas devem rodar em paralelo ou em sequência (onde a avaliação do imóvel só inicia se a análise documental for aprovada).

## Opções Consideradas

### Opção 1: Processamento em Paralelo (Modelo Atual do README)
Ao publicar o evento `PropostaCriadaEvent`, ambas as etapas iniciam simultaneamente.

*   **Prós:**
    *   **Lead Time Mínimo (Velocidade):** O tempo total da jornada é reduzido ao máximo. A vistoria física (que costuma demorar por exigir deslocamento de um engenheiro) ocorre concorrentemente com a checagem burocrática dos documentos.
    *   **Desacoplamento de Domínio:** O serviço de avaliação e o de documentação reagem ao mesmo evento inicial de forma totalmente independente.
*   **Contras:**
    *   **Desperdício Financeiro (Custo):** A avaliação física do imóvel gera um custo operacional direto (pagamento do laudo ao engenheiro). Se o cliente for reprovado na análise de documentos (ex: restrição grave no CPF), o banco terá gasto recursos com a vistoria desnecessariamente.

### Opção 2: Processamento Sequencial (Pipeline Condicional)
A análise documental roda primeiro. Se aprovada, o `analise-documental-service` publica um evento `DocumentacaoAprovadaEvent`, que engatilha o início da avaliação do imóvel.

*   **Prós:**
    *   **Eficiência de Custos:** A vistoria de engenharia só é contratada e executada para clientes cujo crédito e documentação já foram pré-aprovados.
    *   **Fluxo de Negócio Seguro:** Evita retrabalho e processamento inútil na esteira.
*   **Contras:**
    *   **Maior Tempo de Espera (Gargalo Temporal):** Se a validação documental demorar 3 dias e a vistoria mais 5 dias, o cliente esperará no mínimo 8 dias. Em paralelo, a resposta final seria dada em 5 dias.

## Decisão Proposta
Ambas as abordagens são válidas e aceitas no mercado. A escolha depende da prioridade do produto:                                        
                            
* Se vocês preferirem focar em velocidade de entrega para o cliente (e justificar que o banco assume o risco do custo da vistoria em prol
de uma experiência ágil), mantenham o fluxo Paralelo.                                                                                    
* Se preferirem focar em segurança financeira e economia operacional, mudem para o fluxo Sequencial.  

Considerando que o foco do produto é a **experiência do cliente (Lead Time reduzido)**, a **Opção 1 (Paralela)** faz sentido arquiteturalmente para sistemas de alta performance. 

Contudo, para cenários onde a taxa de reprovação documental é muito alta, a **Opção 2 (Sequencial)** é preferível comercialmente. 

*O grupo deve debater e definir qual modelo adotará para o escopo final do projeto.*

## Ajustes conforme a decisão                             
Audança é muito simples de fazer na arquitetura de eventos: bastaria fazer o consumidor do  avaliacao-imovel-service escutar um tópico `documentacao-aprovada`  em vez do tópico  `proposta-criada`.      
