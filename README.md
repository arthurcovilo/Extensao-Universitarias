# Extensão Universitária - App de Gestão de Eventos

Aplicativo Android para gestão de eventos de extensão universitária, desenvolvido para ONGs e instituições de ensino.

## 📱 Funcionalidades

### Para Usuários (Voluntários)
- ✅ Login com email/senha ou Google
- ✅ Visualizar lista de eventos disponíveis
- ✅ Ver detalhes completos dos eventos
- ✅ Inscrever-se em eventos abertos
- ✅ Visualizar perfil com dados pessoais

### Para Administradores
- ✅ Todas as funcionalidades de usuário
- ✅ Criar novos eventos
- ✅ Editar eventos existentes
- ✅ Alterar status (ABERTO/ENCERRADO/CANCELADO)
- ✅ Excluir eventos
- ✅ Ver lista de inscritos com nome e email
- ✅ Gerenciar limite de participantes

## 🛠️ Tecnologias

### Frontend (Android)
- Java
- Android SDK
- RecyclerView
- Material Design
- Google Sign-In (OAuth 2.0)
- Credentials API

### Backend
- Node.js + Express
- PostgreSQL (Neon)
- JWT para autenticação
- bcrypt para hash de senhas
- google-auth-library para validação OAuth

## 🚀 Como Configurar

### Para Membros da Equipe
Siga o guia completo em **[SETUP_EQUIPE.md](SETUP_EQUIPE.md)**

### Resumo Rápido
```bash
# 1. Clonar
git clone https://github.com/arthurcovilo/Extensao-Universitarias.git

# 2. Backend
cd backend
npm install
# Pedir .env.shared para a equipe e copiar para .env
node server.js

# 3. Android Studio
# Abrir o projeto e rodar no emulador
```

## 👥 Usuários de Teste

- **Admin:** `admin@email.com` / `123456`
- **User:** `teste@email.com` / `123456`

## 📂 Estrutura do Projeto

```
Extensao-Universitarias/
├── app/                          # Código Android
│   └── src/main/
│       ├── java/com/example/extensao/
│       │   ├── LoginActivity.java
│       │   ├── EventosActivity.java
│       │   ├── AdminEventActivity.java
│       │   ├── EventDetailActivity.java
│       │   ├── PerfilActivity.java
│       │   ├── AuthApiClient.java
│       │   ├── EventApiClient.java
│       │   └── SessionManager.java
│       └── res/                  # Layouts e recursos
├── backend/
│   ├── server.js                 # API REST
│   ├── setup_db.js               # Script de criação do banco
│   ├── package.json
│   ├── .env.example              # Template de variáveis
│   └── .env.shared               # Credenciais compartilhadas (não no git)
└── SETUP_EQUIPE.md               # Guia de configuração
```

## 🔐 Sistema de Permissões

O app implementa controle de acesso baseado em roles:

- **USER:** Pode visualizar e se inscrever em eventos
- **ADMIN:** Pode gerenciar eventos e ver inscritos

A validação é feita tanto no frontend (UX) quanto no backend (segurança).

## 🗄️ Banco de Dados

### Tabelas

**users**
- id, name, email, password_hash, role (USER/ADMIN), created_at

**events**
- id, title, description, event_date, location, status, max_participants, created_by, created_at

**event_registrations**
- id, user_id, event_id, registered_at
- Constraint: UNIQUE(user_id, event_id)

## 📡 API Endpoints

### Autenticação
- `POST /auth/login` - Login com email/senha
- `POST /auth/google` - Login com Google OAuth

### Eventos (públicos)
- `GET /events` - Listar todos os eventos

### Eventos (autenticados)
- `POST /events/:id/register` - Inscrever-se em evento

### Eventos (apenas ADMIN)
- `POST /events` - Criar evento
- `PUT /events/:id` - Atualizar evento
- `DELETE /events/:id` - Excluir evento
- `GET /events/:id/registrations` - Ver inscritos

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📝 Licença

Este projeto foi desenvolvido para fins educacionais e uso por ONGs.

## 👨‍💻 Equipe

Desenvolvido por estudantes de Engenharia de Software para projeto de extensão universitária.

---

**Dúvidas?** Consulte o [SETUP_EQUIPE.md](SETUP_EQUIPE.md) ou entre em contato com a equipe.
