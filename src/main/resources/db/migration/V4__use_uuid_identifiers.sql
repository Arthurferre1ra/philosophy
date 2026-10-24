-- Novas chaves são geradas uma vez por registro; os vínculos são copiados
-- antes da remoção das chaves numéricas. O Flyway executa tudo em uma transação.
LOCK TABLE periodo_historico, escola_pensamento, filosofo, filosofo_escola IN ACCESS EXCLUSIVE MODE;

ALTER TABLE periodo_historico ADD COLUMN id_uuid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE escola_pensamento ADD COLUMN id_uuid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE filosofo ADD COLUMN id_uuid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE filosofo ADD COLUMN periodo_uuid UUID;
ALTER TABLE filosofo_escola ADD COLUMN filosofo_uuid UUID;
ALTER TABLE filosofo_escola ADD COLUMN escola_uuid UUID;

UPDATE filosofo f SET periodo_uuid = p.id_uuid
FROM periodo_historico p WHERE f.periodo_id = p.id;

UPDATE filosofo_escola fe SET filosofo_uuid = f.id_uuid, escola_uuid = e.id_uuid
FROM filosofo f, escola_pensamento e
WHERE fe.filosofo_id = f.id AND fe.escola_id = e.id;

ALTER TABLE filosofo DROP CONSTRAINT fk_filosofo_periodo;
ALTER TABLE filosofo_escola DROP CONSTRAINT fk_filosofo_escola_filosofo;
ALTER TABLE filosofo_escola DROP CONSTRAINT fk_filosofo_escola_escola;

-- Os índices e sequências vinculados às colunas antigas são removidos junto delas.
ALTER TABLE filosofo_escola DROP COLUMN filosofo_id;
ALTER TABLE filosofo_escola DROP COLUMN escola_id;
ALTER TABLE filosofo DROP COLUMN periodo_id;
ALTER TABLE filosofo DROP COLUMN id;
ALTER TABLE periodo_historico DROP COLUMN id;
ALTER TABLE escola_pensamento DROP COLUMN id;

ALTER TABLE periodo_historico RENAME COLUMN id_uuid TO id;
ALTER TABLE escola_pensamento RENAME COLUMN id_uuid TO id;
ALTER TABLE filosofo RENAME COLUMN id_uuid TO id;
ALTER TABLE filosofo RENAME COLUMN periodo_uuid TO periodo_id;
ALTER TABLE filosofo_escola RENAME COLUMN filosofo_uuid TO filosofo_id;
ALTER TABLE filosofo_escola RENAME COLUMN escola_uuid TO escola_id;

ALTER TABLE periodo_historico ADD PRIMARY KEY (id);
ALTER TABLE escola_pensamento ADD PRIMARY KEY (id);
ALTER TABLE filosofo ADD PRIMARY KEY (id);
ALTER TABLE filosofo_escola ALTER COLUMN filosofo_id SET NOT NULL;
ALTER TABLE filosofo_escola ALTER COLUMN escola_id SET NOT NULL;
ALTER TABLE filosofo_escola ADD PRIMARY KEY (filosofo_id, escola_id);

ALTER TABLE filosofo ADD CONSTRAINT fk_filosofo_periodo
    FOREIGN KEY (periodo_id) REFERENCES periodo_historico(id) ON DELETE SET NULL;
ALTER TABLE filosofo_escola ADD CONSTRAINT fk_filosofo_escola_filosofo
    FOREIGN KEY (filosofo_id) REFERENCES filosofo(id) ON DELETE CASCADE;
ALTER TABLE filosofo_escola ADD CONSTRAINT fk_filosofo_escola_escola
    FOREIGN KEY (escola_id) REFERENCES escola_pensamento(id) ON DELETE CASCADE;

CREATE INDEX idx_filosofo_periodo ON filosofo (periodo_id);
CREATE INDEX idx_filosofo_escola_escola ON filosofo_escola (escola_id);
CREATE INDEX idx_filosofo_criado_id ON filosofo (criado_em DESC, id DESC);
