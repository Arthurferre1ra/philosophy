# Atlas da Filosofia

## Integrantes

- Arthur Ferreira de Melo
- Pedro Roberto Lemos

## Descrição

Atlas da Filosofia é uma aplicação web para consultar filósofos, escolas de pensamento e períodos históricos. O projeto reaproveita o protótipo visual feito em HTML, CSS e JavaScript e o integra com Java, Spring Boot e Thymeleaf.

A aplicação usa PostgreSQL com pool HikariCP e migrações Flyway. Controllers MVC entregam as páginas Thymeleaf, o serviço delimita as transações e os repositórios JDBC executam SQL parametrizado.

## Tecnologias Utilizadas

- Java 21
- Spring Boot
- Spring Web
- Thymeleaf
- PostgreSQL
- Spring JDBC
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
- `@Repository` identifica a camada de persistência, com `NamedParameterJdbcTemplate` e parâmetros nomeados. Os upserts devolvem o ID em uma operação e tratam cadastros concorrentes com o mesmo nome.
- As classes em `model` são modelos de leitura para os templates. O projeto usa JDBC, portanto não requer anotações JPA como `@Entity` ou `@Table`.
- O dashboard consulta cada coleção uma vez; a listagem consulta somente filósofos. As escolas de cada filósofo são agregadas em SQL, sem consultas adicionais por registro.
- Anos de vida e cor visual são persistidos em `filosofo.anos_vida` e `filosofo.cor`. Quando ausentes em registros legados, a exibição usa datas e cores derivadas.

## Migrações do banco

O Flyway aplica os scripts em `src/main/resources/db/migration` uma única vez e verifica seus checksums nas próximas inicializações:

1. `V1`: tabelas e relacionamentos do schema original.
2. `V2`: dados iniciais, sem substituir descrições existentes.
3. `V3`: anos de vida, cor e índices de relacionamento/ordenação.

Em um banco vazio, basta iniciar a aplicação. Para um banco existente criado pelos antigos `db/schema.sql` e `db/data.sql`, confira que ele possui as quatro tabelas originais e use o baseline da versão 1 na primeira inicialização:

```powershell
$env:DB_BASELINE_ON_MIGRATE = "true"
.\mvnw.cmd spring-boot:run
# Depois que a aplicação iniciar, encerre-a e remova a variável:
Remove-Item Env:DB_BASELINE_ON_MIGRATE
```

O baseline registra o schema existente na versão 1, preserva as tabelas e aplica V2/V3. Ele fica desativado por padrão para evitar assumir que um schema desconhecido corresponde ao projeto. Não edite migrações já aplicadas; crie uma nova versão para alterações futuras. A inicialização por `spring.sql.init` fica desativada.

## Configuração da conexão

As credenciais `postgres/postgres` são valores de desenvolvimento local. Para produção, ative `SPRING_PROFILES_ACTIVE=prod` e forneça `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, sem valores padrão.

O pool aceita ajustes por `DB_POOL_SIZE` (10), `DB_POOL_MIN_IDLE` (2) e `DB_CONNECTION_TIMEOUT` (10000 ms). O driver PostgreSQL é detectado pela URL JDBC.

## Respostas dos endpoints

- Consultas válidas: HTTP 200.
- Cadastro válido: redirecionamento para `/filosofos` com mensagem de sucesso.
- Cadastro inválido: HTTP 400, formulário preenchido e mensagens de validação.
- ID inválido (texto, zero ou negativo): HTTP 400.
- Filósofo inexistente: HTTP 404 via `@ResponseStatus`.
- Métodos HTTP não suportados: HTTP 405.

## Testes

```powershell
.\mvnw.cmd test
```

A suíte padrão usa H2 isolado para carregar o contexto e verificar consultas de leitura, além de MockMvc para endpoints, validação e renderização dos templates. Ela não acessa o PostgreSQL da aplicação. H2 não substitui a verificação do SQL específico do PostgreSQL.

Para testar migrações, cadastro completo, upserts concorrentes e rollback em PostgreSQL, disponibilize um **banco de testes** e configure:

```powershell
$env:PG_TEST_URL = "jdbc:postgresql://localhost:5432/philosophy_test"
$env:PG_TEST_USERNAME = "postgres"
$env:PG_TEST_PASSWORD = "postgres"
.\mvnw.cmd test
```

Esses testes são habilitados somente quando `PG_TEST_URL` está definida. Cada teste cria um schema com nome aleatório, aplica as migrações nele e remove esse schema ao terminar. O usuário de teste precisa poder criar schemas.

As páginas devem ser acessadas pelo servidor Spring Boot, e não abrindo arquivos `.html` diretamente.
