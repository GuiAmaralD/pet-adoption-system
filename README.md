# Adoption API

API REST em Spring Boot para um sistema de adocao de pets. A API cobre cadastro e autenticacao de usuarios, cadastro de pets com imagens e filtros de busca. O projeto utiliza Spring Web, Spring Data JPA, Spring Security e validacao com Bean Validation. As imagens sao armazenadas no Supabase Storage. Ha testes unitarios e de integracao com JUnit 5, Mockito e Spring Test.

## Requisitos
- Java 17
- Maven (ou usar `./mvnw`)
- PostgreSQL

## Como rodar local
1. Configure as variaveis de ambiente exigidas (veja `INFRA.md`).
2. Suba o banco de dados.
3. Rode a aplicacao:
   ```bash
   ./mvnw spring-boot:run
   ```

## Docker
Build da imagem:
```bash
docker build -t adoption-api:latest .
```

Executar container:
```bash
docker run --rm -p 8080:8080 --env-file .env adoption-api:latest
```

Observacões:
- Use `--env-file .env` (ou `-e CHAVE=VALOR`) para passar as variaveis de ambiente exigidas pela aplicacao.
- Garanta que o PostgreSQme eacessivel a partir do container.

## Testes
Rodar testes unitarios e de integracao:
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
- Usuarios:
  - `GET /user/{id}`

## Documentacao adicional
- Infraestrutura e variaveis de ambiente: `INFRA.md`
- Endpoints detalhados, payloads, exemplos e respostas:
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Licenca
MIT. Veja `LICENSE.md`.
