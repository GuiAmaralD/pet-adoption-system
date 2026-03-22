# Infraestrutura

Este documento descreve a infraestrutura mínima para executar a API localmente e em ambientes reais.

## Variáveis de ambiente
Estas variáveis são usadas em `src/main/resources/application.yml`.

- `DB_URL`
  - Exemplo: `jdbc:postgresql://localhost:5432/adoption`
- `DB_USER`
  - Exemplo: `postgres`
- `DB_PASS`
  - Exemplo: `postgres`
- `JWT_SECRET`
  - Segredo para assinar tokens JWT. Exemplo: `super-secret`

Variáveis adicionais (Supabase):
- `SUPABASE_URL`
  - Base URL do projeto Supabase. Exemplo: `https://<project>.supabase.co`
- `SUPABASE_KEY`
  - Service key ou anon key com permissão de acesso ao Storage.

## Banco de dados e Flyway
- Banco principal: PostgreSQL
- Banco de testes: H2 em memória (`application-test.yml`)
- O schema é gerenciado por migrations SQL com Flyway
- Pasta das migrations: `src/main/resources/db/migration`



Configuração relevante:
- `spring.jpa.hibernate.ddl-auto: validate`
- `spring.flyway.baseline-on-migrate: true`

Observações:
- Com `ddl-auto: validate`, o Hibernate apenas valida o schema e não altera tabelas.
- Evoluções de banco devem ser feitas por novas migrations.
- `baseline-on-migrate` ajuda quando o banco já existe sem histórico prévio do Flyway.

## Storage de imagens (Supabase)
O upload e delete de imagens usa o Supabase Storage via HTTP.

Bucket esperado:
- `pet-images`

Operações realizadas:
- Upload: `PUT /storage/v1/object/{bucket}/{path}`
- Delete: `DELETE /storage/v1/object/{bucket}/{path}`

## Segurança e autenticação
- JWT assinado via `JWT_SECRET`
- Endpoints públicos:
  - `POST /auth/login`
  - `POST /auth/register`
  - `GET /pet/**`
  - `GET /user/**`
  - Swagger (quando habilitado)
- Demais endpoints exigem `Authorization: Bearer <token>`
