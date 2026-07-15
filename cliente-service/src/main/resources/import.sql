-- Inserts de teste para carregar CPFs válidos na inicialização do H2
CREATE TABLE Cliente (
    cpf VARCHAR(255) NOT NULL,
    nome VARCHAR(255),
    ocupacao VARCHAR(255),
    renda NUMERIC(38, 2),
    PRIMARY KEY (cpf)
);


INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('12345678900', 'Gabriel Silva', 5500.00, 'Analista de TI');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('98765432100', 'Maria Souza', 8200.00, 'Engenheira Civil');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('11122233344', 'Lucas Oliveira', 3500.00, 'Assistente Administrativo');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('22233344455', 'Amanda Santos', 9500.00, 'Gerente de Projetos');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('33344455566', 'Roberto Costa', 4200.00, 'Designer Grafico');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('44455566677', 'Carolina Lima', 12000.00, 'Diretora Executiva');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('55566677788', 'Fernando Rocha', 6700.00, 'Desenvolvedor Senior');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('66677788899', 'Patricia Gomes', 5000.00, 'Analista de Marketing');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('77788899900', 'Bruno Martins', 3100.00, 'Tecnico de Suporte');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('88899900011', 'Juliana Alves', 7200.00, 'Coordenadora de RH');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('99900011122', 'Marcelo Vieira', 8900.00, 'Analista Financeiro');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('12312312312', 'Camila Ribeiro', 6000.00, 'Arquiteta');
INSERT INTO Cliente (cpf, nome, renda, ocupacao) VALUES ('32132132132', 'Rodrigo Barbosa', 4800.00, 'Professor');
