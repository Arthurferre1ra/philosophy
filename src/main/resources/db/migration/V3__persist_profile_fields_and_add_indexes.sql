ALTER TABLE filosofo ADD COLUMN anos_vida VARCHAR(100);
ALTER TABLE filosofo ADD COLUMN cor VARCHAR(20);

ALTER TABLE filosofo ADD CONSTRAINT chk_filosofo_cor
    CHECK (cor IS NULL OR cor IN ('', 'blue', 'green', 'rose', 'lilac', 'terracotta'));

CREATE INDEX idx_filosofo_periodo ON filosofo (periodo_id);
CREATE INDEX idx_filosofo_escola_escola ON filosofo_escola (escola_id);
CREATE INDEX idx_filosofo_criado_id ON filosofo (criado_em DESC, id DESC);
