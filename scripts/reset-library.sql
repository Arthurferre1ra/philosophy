-- Executar apenas no banco da aplicação que deve ser esvaziado.
-- Preserva tabelas, constraints, índices e o histórico das migrações Flyway.
BEGIN;
TRUNCATE TABLE filosofo_escola, filosofo, escola_pensamento, periodo_historico;
COMMIT;
