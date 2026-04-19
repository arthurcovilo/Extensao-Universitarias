require('dotenv').config();

const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');
const { OAuth2Client } = require('google-auth-library');

const app = express();
app.use(express.json());

const WEB_CLIENT_ID = '487621614650-ekd8795v6uu6ac2uco886h95f09lrta4.apps.googleusercontent.com';
const googleClient = new OAuth2Client(WEB_CLIENT_ID);

// ── Conexão com o banco ──────────────────────────────────────────────────────
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

pool.connect()
  .then(() => console.log('✅ Conectado ao PostgreSQL'))
  .catch(err => {
    console.error('❌ Falha ao conectar ao PostgreSQL:', err.message);
    console.error('   Verifique DATABASE_URL no .env e se o banco está rodando.');
  });

// ── POST /auth/login ─────────────────────────────────────────────────────────
app.post('/auth/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: 'Email e senha são obrigatórios' });
  }

  try {
    const result = await pool.query(
      'SELECT id, name, email, password_hash FROM users WHERE email = $1',
      [email.toLowerCase().trim()]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ message: 'Credenciais inválidas' });
    }

    const user = result.rows[0];
    const senhaCorreta = await bcrypt.compare(password, user.password_hash);

    if (!senhaCorreta) {
      return res.status(401).json({ message: 'Credenciais inválidas' });
    }

    const accessToken = jwt.sign(
      { sub: user.id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(200).json({
      accessToken,
      user: {
        name: user.name,
        email: user.email,
      },
    });

  } catch (err) {
    console.error('Erro no login:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── POST /auth/google ─────────────────────────────────────────────────────────
app.post('/auth/google', async (req, res) => {
  const { idToken } = req.body;

  if (!idToken) {
    return res.status(400).json({ message: 'idToken é obrigatório' });
  }

  try {
    // Valida o token com o Google
    const ticket = await googleClient.verifyIdToken({
      idToken,
      audience: WEB_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const googleEmail = payload.email.toLowerCase().trim();
    const googleName = payload.name || googleEmail;

    // Busca ou cria o usuário no banco
    let result = await pool.query(
      'SELECT id, name, email FROM users WHERE email = $1',
      [googleEmail]
    );

    let user;
    if (result.rows.length === 0) {
      // Primeiro acesso — cria o usuário sem senha
      const insert = await pool.query(
        'INSERT INTO users (name, email, password_hash) VALUES ($1, $2, $3) RETURNING id, name, email',
        [googleName, googleEmail, '']
      );
      user = insert.rows[0];
    } else {
      user = result.rows[0];
    }

    const accessToken = jwt.sign(
      { sub: user.id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(200).json({
      accessToken,
      user: {
        name: user.name,
        email: user.email,
      },
    });

  } catch (err) {
    console.error('Erro no login Google:', err.message);
    return res.status(401).json({ message: 'Token Google inválido' });
  }
});

// ── Health check ─────────────────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

// ── Start ─────────────────────────────────────────────────────────────────────
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`🚀 Backend rodando em http://localhost:${PORT}`);
});
