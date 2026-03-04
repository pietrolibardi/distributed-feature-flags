# Arquitetura – Distributed Feature Flag Platform

Este documento descreve as decisões técnicas e padrões adotados na plataforma.

---

## Stack e Runtime

| Decisão | Detalhe |
|--------|---------|
| **Linguagem** | Java 21 (LTS) |
| **Framework** | Spring Boot 3.x |
| **Build** | Maven |

---

## Persistência

- **PostgreSQL por serviço**: cada microserviço possui seu próprio banco PostgreSQL, garantindo independência de dados e evolução de schema isolada.

---

## Comunicação entre Serviços

### Fase 1 (atual)

- **REST síncrona**: os serviços se comunicam via HTTP/REST. Chamadas diretas entre serviços para operações que exigem resposta imediata.

### Fase 2 (planejada)

- **Mensageria (Kafka)**: introdução de eventos assíncronos para propagação de mudanças (ex.: criação/atualização de flags), desacoplamento e maior escalabilidade.

---

## Autenticação e Identidade

- **JWT (JSON Web Token)** para autenticação:
  - Emissão e validação centralizadas (Auth Service).
  - Tokens validados no API Gateway antes do roteamento.
  - Sem sessão server-side; stateless.

---

## Identificadores

- **UUID como ID padrão**: entidades expostas na API e em eventos utilizam UUID como identificador único (ex.: `id` de flags, usuários, projetos). Evita sequenciais previsíveis e facilita geração distribuída.

---

## Padrão de Resposta da API

Todas as APIs REST seguem um formato único de resposta para facilitar o consumo pelo cliente.

### Sucesso (2xx)

```json
{
  "success": true,
  "data": { ... },
  "message": "Operação realizada com sucesso"
}
```

- **success**: `true`
- **data**: payload da operação (objeto ou array)
- **message**: (opcional) mensagem descritiva

### Erro (4xx / 5xx)

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Descrição legível do erro",
    "details": []
  },
  "timestamp": "2025-03-03T12:00:00Z",
  "path": "/api/v1/flags"
}
```

- **success**: `false`
- **error.code**: código interno do erro (ex.: `VALIDATION_ERROR`, `NOT_FOUND`, `UNAUTHORIZED`)
- **error.message**: mensagem legível para o cliente
- **error.details**: (opcional) lista de erros de validação ou detalhes adicionais
- **timestamp**: instante do erro (ISO 8601)
- **path**: caminho da requisição que gerou o erro

### Paginação (quando aplicável)

Para listagens paginadas, `data` segue o formato:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## Resumo das Decisões

| Área | Decisão |
|------|---------|
| Runtime | Java 21, Spring Boot 3.x, Maven |
| Banco de dados | PostgreSQL por serviço |
| Comunicação (fase 1) | REST síncrona |
| Comunicação (fase 2) | Kafka para eventos assíncronos |
| Autenticação | JWT |
| Identificadores | UUID como ID padrão |
| Contratos da API | Padrão de resposta único (success, data/error, message, etc.) |
