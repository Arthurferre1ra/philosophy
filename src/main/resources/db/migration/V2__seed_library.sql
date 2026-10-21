INSERT INTO periodo_historico (nome, descricao, ano_inicio, ano_fim)
VALUES
    ('Antiguidade', 'Do nascimento da filosofia grega à queda do Império Romano. A investigação passa pela natureza, pela cidade e pela vida virtuosa.', -600, 476),
    ('Período Medieval', 'Fé, razão e metafísica se encontram em debates sobre existência, ética e a relação entre o ser humano e o divino.', 476, 1453),
    ('Modernidade', 'A razão, a ciência e a autonomia individual ganham protagonismo na formação do pensamento moderno.', 1453, 1789),
    ('Contemporânea', 'Novas perspectivas questionam linguagem, sociedade, existência e estruturas que organizam a vida comum.', 1789, NULL)
ON CONFLICT (nome) DO NOTHING;

INSERT INTO escola_pensamento (nome, descricao, origem, ano_inicio, ano_fim)
VALUES
    ('Aristotelismo', 'Investigação sobre lógica, natureza, ética e política a partir da obra de Aristóteles.', 'Antiguidade', -384, NULL),
    ('Estoicismo', 'Uma filosofia prática sobre serenidade, virtude e a distinção entre aquilo que podemos ou não controlar.', 'Antiguidade', -300, 300),
    ('Existencialismo', 'Coloca liberdade, responsabilidade e sentido no centro da experiência humana concreta.', 'Século XX', 1900, 1999),
    ('Filosofia clássica', 'Tradição grega marcada pelo diálogo, pela ética e pela investigação racional da vida comum.', 'Antiguidade', -500, -300),
    ('Idealismo', 'Explora a relação entre realidade, conhecimento e as formas que estruturam nossa compreensão do mundo.', 'Antiguidade', -428, NULL),
    ('Iluminismo', 'Movimento intelectual moderno voltado à autonomia da razão, crítica e progresso.', 'Modernidade', 1685, 1815),
    ('Racionalismo', 'Defende a razão como fonte central do conhecimento e investiga os limites da dúvida e da certeza.', 'Modernidade', 1600, 1750)
ON CONFLICT (nome) DO NOTHING;

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'Sócrates', 'Sócrates', NULL, NULL, 'Grego', 'Filósofo ateniense que colocou o diálogo e a pergunta no centro da investigação ética.', 'Só sei que nada sei.', p.id
FROM periodo_historico p
WHERE p.nome = 'Antiguidade'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'Sócrates');

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'Platão', 'Platão', NULL, NULL, 'Grego', 'Fundador da Academia, pensou justiça, conhecimento e política a partir da teoria das ideias.', 'A direção em que a educação inicia uma pessoa determinará o seu futuro.', p.id
FROM periodo_historico p
WHERE p.nome = 'Antiguidade'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'Platão');

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'Aristóteles', 'Aristóteles', NULL, NULL, 'Grego', 'Investigou lógica, ética, natureza e política, defendendo a busca pela virtude como prática.', 'A excelência não é um ato, mas um hábito.', p.id
FROM periodo_historico p
WHERE p.nome = 'Antiguidade'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'Aristóteles');

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'René Descartes', 'René Descartes', '1596-03-31', '1650-02-11', 'Francês', 'Marco da filosofia moderna, propôs a dúvida metódica e buscou fundamentos seguros para o conhecimento.', 'Penso, logo existo.', p.id
FROM periodo_historico p
WHERE p.nome = 'Modernidade'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'René Descartes');

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'Immanuel Kant', 'Immanuel Kant', '1724-04-22', '1804-02-12', 'Prussiano', 'Analisou os limites da razão e formulou uma ética baseada na autonomia e no dever.', 'Ouse saber! Tenha coragem de usar seu próprio entendimento.', p.id
FROM periodo_historico p
WHERE p.nome = 'Modernidade'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'Immanuel Kant');

INSERT INTO filosofo (nome, nome_completo, data_nascimento, data_falecimento, nacionalidade, biografia, principais_ideias, periodo_id)
SELECT 'Simone de Beauvoir', 'Simone Lucie Ernestine Marie Bertrand de Beauvoir', '1908-01-09', '1986-04-14', 'Francesa', 'Pensadora existencialista que relacionou liberdade, opressão e construção histórica da identidade.', 'Não se nasce mulher: torna-se mulher.', p.id
FROM periodo_historico p
WHERE p.nome = 'Contemporânea'
  AND NOT EXISTS (SELECT 1 FROM filosofo WHERE nome = 'Simone de Beauvoir');

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Filosofia clássica'
WHERE f.nome = 'Sócrates'
ON CONFLICT DO NOTHING;

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Idealismo'
WHERE f.nome = 'Platão'
ON CONFLICT DO NOTHING;

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Aristotelismo'
WHERE f.nome = 'Aristóteles'
ON CONFLICT DO NOTHING;

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Racionalismo'
WHERE f.nome = 'René Descartes'
ON CONFLICT DO NOTHING;

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Iluminismo'
WHERE f.nome = 'Immanuel Kant'
ON CONFLICT DO NOTHING;

INSERT INTO filosofo_escola (filosofo_id, escola_id)
SELECT f.id, e.id
FROM filosofo f
JOIN escola_pensamento e ON e.nome = 'Existencialismo'
WHERE f.nome = 'Simone de Beauvoir'
ON CONFLICT DO NOTHING;
