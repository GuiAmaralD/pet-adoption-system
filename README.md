# Adoption API

API REST em Spring Boot para um sistema de adoção de pets. A API cobre cadastro e autenticação de usuários, cadastro de pets com imagens e filtros de busca. O projeto utiliza Spring Web, Spring Data JPA, Flyway, Spring Security e validação com Bean Validation. As imagens são armazenadas no Supabase Storage. Há testes unitários e de integração com JUnit 5, Mockito e Spring Test.

## Requisitos
- Java 17
- Maven (ou usar `./mvnw`)
- PostgreSQL

## Como rodar local
1. Configure as variáveis de ambiente exigidas (veja `INFRA.md`).
2. Suba o banco de dados.
3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

Ao iniciar, o Flyway aplica automaticamente as migrations. Detalhes de banco e migrações estão no `INFRA.md`.

## Docker
Build da imagem:
```bash
docker build -t adoption-api:latest .
```

Executar container:
```bash
docker run --rm -p 8080:8080 --env-file .env adoption-api:latest
```

Observações:
- Use `--env-file .env` (ou `-e CHAVE=VALOR`) para passar as variáveis de ambiente exigidas pela aplicação.
- Garanta que o PostgreSQL esteja acessível a partir do container.

## Testes
Rodar testes unitários e de integração:
```bash
./mvnw test
```

## Estrutura de endpoints (resumo)
- Auth:
  - `POST /auth/register`
  - `POST /auth/login`
- Conta:
  - `GET /account/me`
  - `PUT /account`
  - `PUT /account/password`
  - `DELETE /account`
- Pets:
  - `GET /pet`
  - `GET /pet/filter`
  - `GET /pet/{id}`
  - `POST /pet` (multipart)
  - `PUT /pet/{id}`
  - `PUT /pet/{id}/adopted`
  - `DELETE /pet/{id}`
- Usuários:
  - `GET /user/{id}`

## Documentação adicional
- Infraestrutura e variáveis de ambiente: `INFRA.md`
- Endpoints detalhados, payloads, exemplos e respostas:
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Licença
MIT. Veja `LICENSE.md`.
