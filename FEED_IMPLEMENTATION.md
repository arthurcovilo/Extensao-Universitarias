# Feed de avisos — Extensão Universitária

O Feed é a tela principal após o login. O admin publica avisos (só texto) e os voluntários os veem no app.

## Backend (`backend/`)

Tabela `posts`:

| Coluna | Tipo | Observação |
|---|---|---|
| `id` | `SERIAL PRIMARY KEY` | |
| `title` | `VARCHAR(255) NOT NULL` | |
| `content` | `TEXT NOT NULL` | até 5000 caracteres (validado na API) |
| `author_id` | `INTEGER REFERENCES users(id) ON DELETE SET NULL` | admin que publicou |
| `created_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | índice `idx_posts_created_at` |

- Banco novo: `node setup_db.js` já cria a tabela.
- Banco existente: `node migrate_posts.js` (só `CREATE ... IF NOT EXISTS`, pode rodar mais de uma vez). Rode **antes** de subir o servidor com as rotas de posts.

Endpoints (`server.js`):

| Método | Rota | Acesso | Resposta |
|---|---|---|---|
| GET | `/posts?limit=N` | usuário logado | lista do mais novo ao mais antigo; `limit` 1–100, padrão 50 |
| POST | `/posts` | ADMIN | `{ title, content }` → 201 com o post; 400 se inválido (`validatePostInput`) |
| DELETE | `/posts/:id` | ADMIN | 200, 400 (id inválido) ou 404 |

## App (`app/src/main/java/com/example/extensao/`)

- `FeedActivity` — carrega os posts pela API, com estados de carregando, vazio e erro (toque para tentar de novo) e pull-to-refresh. Para o ADMIN, mostra o botão "+ Publicar" no cabeçalho (diálogo `dialog_publicar_post.xml`) e "Excluir" no diálogo de detalhes.
- `PostApiClient` — `getPosts` (lança exceção em erro, para a tela diferenciar erro de lista vazia), `createPost`, `deletePost`; 401/403 passam por `SessionManager.handleUnauthorized`. Converte o JSON em `PostModel` e formata `created_at` como `dd/MM/yyyy HH:mm` no fuso America/Sao_Paulo.
- `PostModel`, `PostAdapter`, `item_post.xml` — card com título, texto (3 linhas), data e botão "Ver detalhes" (abre o texto completo). Os campos de imagem e local existem no layout, mas ficam escondidos na v1.
- Layouts: `activity_feed.xml`, `item_post.xml`, `dialog_publicar_post.xml`. Dependências: RecyclerView, SwipeRefreshLayout, CardView (e Glide, usado por `ImageUtils`).

## Fora da v1
- Imagens nos posts (o disco do Render é efêmero; precisaria de armazenamento externo).
- Edição de post, paginação além do `limit`, notificações push.
