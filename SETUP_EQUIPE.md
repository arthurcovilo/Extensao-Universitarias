# 🚀 Guia de Configuração para a Equipe

Este guia explica como configurar o projeto na sua máquina para trabalhar em equipe.

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Node.js** (versão 18 ou superior) - [Download](https://nodejs.org/)
- **Android Studio** - [Download](https://developer.android.com/studio)
- **Git** - [Download](https://git-scm.com/)

---

## 🔧 Passo a Passo

### 1. Clonar o Repositório

```bash
git clone https://github.com/arthurcovilo/Extensao-Universitarias.git
cd Extensao-Universitarias
```

### 2. Configurar o Backend

#### 2.1 Instalar dependências
```bash
cd backend
npm install
```

#### 2.2 Configurar variáveis de ambiente

**IMPORTANTE:** Peça o arquivo `.env.shared` para o líder da equipe (não está no GitHub por segurança).

Depois de receber o arquivo:

```bash
# No Windows (PowerShell)
Copy-Item .env.shared .env

# No Mac/Linux
cp .env.shared .env
```

#### 2.3 Rodar o backend

```bash
node server.js
```

Você deve ver:
```
🚀 Backend rodando em http://localhost:8080
✅ Conectado ao PostgreSQL
```

**⚠️ IMPORTANTE:** Deixe esse terminal aberto enquanto estiver desenvolvendo. Se fechar, o app para de funcionar.

---

### 3. Configurar o Android Studio

#### 3.1 Abrir o projeto
1. Abra o Android Studio
2. **File → Open**
3. Selecione a pasta `Extensao-Universitarias`

#### 3.2 Sync do Gradle
- Aguarde o Android Studio sincronizar automaticamente
- Se não sincronizar, clique em **File → Sync Project with Gradle Files**

#### 3.3 Rodar no emulador
1. Crie um emulador Android (se não tiver):
   - **Tools → Device Manager → Create Device**
   - Escolha um dispositivo (ex: Pixel 5)
   - Escolha uma API (recomendado: API 33 ou superior)
2. Clique no botão ▶️ **Run**

---

## 👥 Usuários de Teste

O banco já vem com 2 usuários criados:

### Admin (pode criar/editar/excluir eventos)
- **Email:** `admin@email.com`
- **Senha:** `123456`

### Usuário comum (pode apenas se inscrever)
- **Email:** `teste@email.com`
- **Senha:** `123456`

---

## 🔄 Fluxo de Trabalho Diário

### Antes de começar a trabalhar:

```bash
# 1. Atualizar o código
git pull origin main

# 2. Rodar o backend (em um terminal separado)
cd backend
node server.js

# 3. Abrir o Android Studio e rodar o app
```

### Depois de fazer alterações:

```bash
# 1. Adicionar arquivos modificados
git add .

# 2. Fazer commit
git commit -m "descrição do que foi feito"

# 3. Enviar para o GitHub
git push origin main
```

---

## 🗄️ Banco de Dados Compartilhado

**ATENÇÃO:** Todos da equipe usam o **mesmo banco de dados**.

Isso significa que:
- ✅ Eventos criados por você aparecem para todo mundo
- ✅ Inscrições feitas por qualquer pessoa são visíveis para todos
- ⚠️ Se alguém excluir um evento, ele some para todo mundo
- ⚠️ Cuidado ao fazer testes destrutivos

---

## 🐛 Problemas Comuns

### "Falha de conexão. Verifique internet e URL da API"
**Solução:** O backend não está rodando. Abra um terminal e rode:
```bash
cd backend
node server.js
```

### "Erro ao conectar ao PostgreSQL"
**Solução:** Verifique se o arquivo `.env` está configurado corretamente com a connection string do Neon.

### "No credentials available" (Google Sign-In)
**Solução:** Configure uma conta Google no emulador:
1. Abra o app de Configurações do emulador
2. Vá em **Passwords & accounts**
3. **Add account → Google**
4. Faça login com sua conta

### Gradle sync falhou
**Solução:**
1. **File → Invalidate Caches → Invalidate and Restart**
2. Aguarde o Android Studio reiniciar e sincronizar

---

## 📞 Suporte

Se tiver problemas, entre em contato com a equipe no grupo do projeto.

---

## 🔐 Segurança

**NUNCA commite o arquivo `.env` no GitHub!**

Ele contém credenciais sensíveis do banco de dados. O `.gitignore` já está configurado para ignorá-lo, mas sempre verifique antes de fazer push.

---

Bom desenvolvimento! 🚀
