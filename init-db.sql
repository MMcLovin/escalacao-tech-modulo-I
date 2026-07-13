-- Criação das bases segregadas para garantir a decomposição de domínios
CREATE DATABASE analise_db;
CREATE DATABASE avaliacao_db;

-- Permissões adicionais se necessário
GRANT ALL PRIVILEGES ON DATABASE analise_db TO "user";
GRANT ALL PRIVILEGES ON DATABASE avaliacao_db TO "user";
