# Atlas da Filosofia

## Integrantes

- Arthur Ferreira
- Integrante 2: preencher com o nome da dupla

## Descrição

Atlas da Filosofia é uma aplicação web para consultar filósofos, escolas de pensamento e períodos históricos. O projeto reaproveita o protótipo visual feito em HTML, CSS e JavaScript e o integra com Java, Spring Boot e Thymeleaf.

Nesta versão, os dados são simulados em memória. A listagem de filósofos, escolas e períodos é enviada pelo Controller para os templates via `Model`, e o cadastro de novos filósofos funciona por formulário integrado ao backend.

## Tecnologias Utilizadas

- Java 21
- Spring Boot
- Spring Web
- Thymeleaf
- HTML
- CSS
- JavaScript
- Maven

## Como Executar

Pré-requisito: ter um JDK 21 configurado no computador.

1. Clone o repositório.
2. Abra a pasta do projeto.
3. Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, também é possível executar:

```bash
mvnw.cmd spring-boot:run
```

4. Acesse no navegador:

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

## Observações

- O projeto não utiliza banco de dados nesta etapa.
- Os registros ficam em memória enquanto a aplicação está rodando.
- As páginas devem ser acessadas pelo servidor Spring Boot, e não abrindo arquivos `.html` diretamente.
