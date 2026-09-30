 
CREATE TABLE periodo_historico (
   id BIGSERIAL PRIMARY KEY,
 
   nome VARCHAR(100) NOT NULL UNIQUE,
 
   descricao TEXT,
 
   ano_inicio INTEGER,
 
   ano_fim INTEGER,
 
   criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   CONSTRAINT chk_periodo_anos
       CHECK (
           ano_fim IS NULL
           OR ano_inicio IS NULL
           OR ano_fim >= ano_inicio
       )
);
 
 
CREATE TABLE escola_pensamento (
   id BIGSERIAL PRIMARY KEY,
 
   nome VARCHAR(150) NOT NULL UNIQUE,
 
   descricao TEXT,
 
   origem VARCHAR(150),
 
   ano_inicio INTEGER,
 
   ano_fim INTEGER,
 
   criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   CONSTRAINT chk_escola_anos
       CHECK (
           ano_fim IS NULL
           OR ano_inicio IS NULL
           OR ano_fim >= ano_inicio
       )
);
 
 
CREATE TABLE filosofo (
   id BIGSERIAL PRIMARY KEY,
 
   nome VARCHAR(150) NOT NULL,
 
   nome_completo VARCHAR(200),
 
   data_nascimento DATE,
 
   data_falecimento DATE,
 
   local_nascimento VARCHAR(150),
 
   nacionalidade VARCHAR(100),
 
   biografia TEXT,
 
   principais_ideias TEXT,
 
   imagem_url VARCHAR(500),
 
   periodo_id BIGINT,
 
   criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 
   CONSTRAINT fk_filosofo_periodo
       FOREIGN KEY (periodo_id)
       REFERENCES periodo_historico(id)
       ON DELETE SET NULL,
 
   CONSTRAINT chk_filosofo_datas
       CHECK (
           data_falecimento IS NULL
           OR data_nascimento IS NULL
           OR data_falecimento >= data_nascimento
       )
);
 
 
 
CREATE TABLE filosofo_escola (
   filosofo_id BIGINT NOT NULL,
 
   escola_id BIGINT NOT NULL,
 
   PRIMARY KEY (filosofo_id, escola_id),
 
   CONSTRAINT fk_filosofo_escola_filosofo
       FOREIGN KEY (filosofo_id)
       REFERENCES filosofo(id)
       ON DELETE CASCADE,
 
   CONSTRAINT fk_filosofo_escola_escola
       FOREIGN KEY (escola_id)
       REFERENCES escola_pensamento(id)
       ON DELETE CASCADE
);
 
 
 
