# Atlas da Filosofia

## Integrantes

- Arthur Ferreira de Melo
- Pedro Roberto Lemos

## Descrição

Atlas da Filosofia é uma aplicação web para consultar filósofos, escolas de pensamento e períodos históricos. O projeto reaproveita o protótipo visual feito em HTML, CSS e JavaScript e o integra com Java, Spring Boot e Thymeleaf.

A aplicação usa PostgreSQL com pool HikariCP e migrações Flyway. Controllers MVC entregam as páginas Thymeleaf, o serviço delimita as transações e os repositórios usam JPA/Hibernate. Os catálogos mantêm upserts SQL parametrizados para tratar cadastros concorrentes.

## Tecnologias Utilizadas

- Java 21
- Lombok
- Spring Boot
- Spring Web
- Thymeleaf
- PostgreSQL
- Spring Data JPA / Hibernate
- Spring JDBC (upserts dos catálogos)
- Jakarta Bean Validation
- Flyway
- HikariCP
- HTML
- CSS
- JavaScript
- Maven

## Como Executar

Pré-requisitos: ter um JDK 21 configurado no computador e um PostgreSQL disponível.

1. Clone o repositório.
2. Abra a pasta do projeto.
3. Suba o banco local com Docker:

```bash
docker compose up -d
```

Por padrão, a aplicação usa:

```text
Banco: philosophy
Usuário: postgres
Senha: postgres
URL: jdbc:postgresql://localhost:5432/philosophy
```

Também é possível sobrescrever esses valores pelas variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

Se preferir usar um PostgreSQL instalado localmente, crie antes um banco chamado `philosophy` ou ajuste `DB_URL` para um banco já existente.

4. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, também é possível executar:

```bash
mvnw.cmd spring-boot:run
```

5. Acesse no navegador:

```text
http://localhost:8080
```

## Principais Rotas

- `GET /` - página inicial/dashboard
- `GET /filosofos` - listagem de filósofos
- `GET /filosofos/novo` - formulário de cadastro
- `POST /filosofos` - envio do formulário para o backend
- `GET /filosofos/{id}` - detalhes de um filósofo
- `GET /escolas` - escolas de pensamento
- `GET /periodos` - períodos históricos

## Persistência e anotações

- `@Controller`, `@RequestMapping`, `@GetMapping` e `@PostMapping` delimitam as rotas MVC. As respostas continuam sendo páginas HTML.
- `@Valid @ModelAttribute` valida o DTO `PhilosopherForm`; o `BindingResult` vem imediatamente depois do DTO. O formulário não aceita identificadores ou campos internos para persistência.
- `@NotBlank`, `@Size` e `@Pattern` verificam campos obrigatórios, limites e cores permitidas. O serviço usa `@Validated` para validar chamadas fora do controller também.
- `@Service` e `@Transactional(readOnly = true)` delimitam as leituras. O cadastro usa `@Transactional`: período, escola, filósofo e vínculo são confirmados juntos ou revertidos juntos.
- `@Repository` identifica a camada de persistência, que usa `EntityManager`. Os upserts de períodos e escolas usam `NamedParameterJdbcTemplate` na mesma transação JPA, devolvendo o ID em uma operação e tratando cadastros concorrentes com o mesmo nome.
- As classes `Philosopher`, `Period` e `School` em `model` são entidades com `@Entity`, `@Table` e `@Column` alinhados ao schema existente. Os modelos de exibição ficam em `dto`, com os campos calculados de iniciais, cores e intervalos históricos.
- `BaseEntity` usa `@MappedSuperclass` e centraliza `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)` e as datas de criação/atualização via `@PrePersist` e `@PreUpdate`. O ID e a data de criação não possuem setters e não podem ser atualizados pelo mapeamento.
- O período do filósofo usa `@ManyToOne` / `@JoinColumn`. Suas escolas usam `@ManyToMany` / `@JoinTable` em `filosofo_escola`, cuja chave primária composta continua sendo o par de UUIDs. O vínculo não tem um ID artificial nem `@GeneratedValue`. Excluir um filósofo não exclui as escolas e os períodos compartilhados.
- O dashboard consulta cada coleção uma vez; a listagem consulta somente filósofos. A consulta JPA usa `JOIN FETCH` para carregar período e escolas juntos, sem consultas adicionais por registro. Os DTOs são construídos dentro da transação, com `spring.jpa.open-in-view=false`.
- `spring.jpa.hibernate.ddl-auto=validate` confere os mapeamentos sem alterar as tabelas. O Flyway continua responsável pelas migrações.
- Anos de vida e cor visual são persistidos em `filosofo.anos_vida` e `filosofo.cor`. Quando ausentes em registros legados, a exibição usa datas e cores derivadas.
- O período pode ser selecionado entre as sugestões ou digitado. Novos períodos e escolas são criados na mesma transação do filósofo, inclusive quando o banco está vazio.
- O JavaScript ativo em `/js/script.js` apenas filtra os registros renderizados pelo servidor. O cadastro envia o formulário ao backend e só mostra sucesso depois da confirmação da transação.

## Migrações do banco

O Flyway aplica os scripts em `src/main/resources/db/migration` uma única vez e verifica seus checksums nas próximas inicializações:

1. `V1`: tabelas e relacionamentos do schema original.
2. `V2`: dados iniciais, sem substituir descrições existentes.
3. `V3`: anos de vida, cor e índices de relacionamento/ordenação.
4. `V4`: IDs UUID de filósofos, escolas e períodos, preservando os registros e relacionamentos existentes.

Os IDs são UUID v4 representados por `java.util.UUID` no backend. Entidades persistidas por JPA recebem o ID pelo Hibernate com `GenerationType.UUID`. Inserções SQL diretas, incluindo os upserts dos catálogos e os dados iniciais, usam o default `gen_random_uuid()` do PostgreSQL. A V4 substitui os IDs numéricos; links antigos devem ser substituídos pelos links atuais das listagens. As migrações existentes permanecem intactas para preservar os checksums do Flyway.

Em um banco vazio, basta iniciar a aplicação. Para um banco existente criado pelos antigos `db/schema.sql` e `db/data.sql`, confira que ele possui as quatro tabelas originais e use o baseline da versão 1 na primeira inicialização:

```powershell
$env:DB_BASELINE_ON_MIGRATE = "true"
.\mvnw.cmd spring-boot:run
# Depois que a aplicação iniciar, encerre-a e remova a variável:
Remove-Item Env:DB_BASELINE_ON_MIGRATE
```

O baseline registra o schema existente na versão 1, preserva as tabelas e aplica V2/V3/V4. Ele fica desativado por padrão para evitar assumir que um schema desconhecido corresponde ao projeto. Não edite migrações já aplicadas; crie uma nova versão para alterações futuras. A inicialização por `spring.sql.init` fica desativada.

## Configuração da conexão

As credenciais `postgres/postgres` são valores de desenvolvimento local. Para produção, ative `SPRING_PROFILES_ACTIVE=prod` e forneça `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, sem valores padrão.

O pool aceita ajustes por `DB_POOL_SIZE` (10), `DB_POOL_MIN_IDLE` (2) e `DB_CONNECTION_TIMEOUT` (10000 ms). O driver PostgreSQL é detectado pela URL JDBC.

As consultas Hibernate/JDBC e as confirmações/reversões de transação aparecem no console em nível `DEBUG`. Para reduzir os logs, configure `DB_SQL_LOG_LEVEL=INFO` e `DB_TRANSACTION_LOG_LEVEL=INFO`.

## Limpar os registros

O script `scripts/reset-library.sql` esvazia filósofos, vínculos, escolas e períodos. Novos cadastros recebem novos UUIDs. Execute-o somente conectado ao banco da aplicação que deseja limpar. Ele preserva a estrutura e `flyway_schema_history`, para que as migrações já aplicadas não voltem a inserir os dados iniciais na próxima inicialização. O formulário permite cadastrar o primeiro período após a limpeza.

## Respostas dos endpoints

- Consultas válidas: HTTP 200.
- Cadastro válido: redirecionamento para `/filosofos` com mensagem de sucesso.
- Cadastro inválido: HTTP 400, formulário preenchido e mensagens de validação.
- ID com formato UUID inválido: HTTP 400.
- UUID válido de filósofo inexistente: HTTP 404 via `@ResponseStatus`.
- Métodos HTTP não suportados: HTTP 405.

## Testes

```powershell
.\mvnw.cmd test
```

A suíte padrão usa H2 isolado para validar os mapeamentos, a geração automática de UUIDs, os relacionamentos, as datas de auditoria e as consultas de leitura, além de MockMvc para endpoints, validação e renderização dos templates. Ela não acessa o PostgreSQL da aplicação. H2 não substitui a verificação do SQL específico do PostgreSQL.

Para testar migrações, cadastro completo, upserts concorrentes e rollback em PostgreSQL, disponibilize um **banco de testes** e configure:

```powershell
$env:PG_TEST_URL = "jdbc:postgresql://localhost:5432/philosophy_test"
$env:PG_TEST_USERNAME = "postgres"
$env:PG_TEST_PASSWORD = "postgres"
.\mvnw.cmd test
```

Esses testes são habilitados somente quando `PG_TEST_URL` está definida. Cada teste cria um schema com nome aleatório, aplica as migrações nele e remove esse schema ao terminar. O usuário de teste precisa poder criar schemas.

`PostgresRegistrationTests` também envia o formulário por MockMvc com a persistência real e verifica os dados em uma conexão JDBC independente, comprovando o commit. O teste começa com as quatro tabelas vazias e valida o primeiro cadastro, a leitura das páginas e a rejeição de um formulário inválido.

As páginas devem ser acessadas pelo servidor Spring Boot, e não abrindo arquivos `.html` diretamente.
