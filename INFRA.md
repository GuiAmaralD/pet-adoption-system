# Infraestrutura

Este documento descreve a infraestrutura mínima para executar a API localmente e em ambientes reais.

## Variáveis de ambiente
Estas variáveis são usadas em `src/main/resources/application.yml`.

- `DB_URL`  
  Exemplo: `jdbc:postgresql://localhost:5432/adoption`
- `DB_USER`  
  Exemplo: `postgres`
- `DB_PASS`  
  Exemplo: `postgres`
- `JWT_SECRET`  
  Segredo para assinar tokens JWT. Exemplo: `super-secret`

Variáveis de ambiente adicionais (Supabase):
- `SUPABASE_URL`  
  Base URL do projeto Supabase. Exemplo: `https://<project>.supabase.co`
- `SUPABASE_KEY`  
  Service key ou anon key com permissão de acesso ao Storage.

## Banco de dados
O projeto usa PostgreSQL em produção e H2 em testes.

Configuração default do Hibernate:
- `ddl-auto: update`
- `show-sql: true`

Para testes (`application-test.yml`):
- banco H2 em memória
- `ddl-auto: create-drop`

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
