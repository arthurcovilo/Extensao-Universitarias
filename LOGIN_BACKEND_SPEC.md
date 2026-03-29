# Login Backend Spec

Este app espera um endpoint HTTP para autenticação.

## Endpoint

- **URL (dev em emulador Android):** `http://10.0.2.2:8080/auth/login`
- **Método:** `POST`
- **Content-Type:** `application/json`

## Request

```json
{
  "email": "usuario@email.com",
  "password": "123456"
}
```

## Success Response (200)

```json
{
  "accessToken": "jwt-ou-token-seguro",
  "user": {
    "name": "Nome do Usuário",
    "email": "usuario@email.com"
  }
}
```

## Error Response (401)

```json
{
  "message": "Credenciais inválidas"
}
```

## Checklist de produção

1. HTTPS habilitado.
2. Senha com hash (bcrypt/argon2).
3. Expiração e renovação de token.
4. Rate limit para tentativas de login.
5. Logs e monitoramento.
