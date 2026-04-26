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
      'SELECT id, name, email, password_hash, role FROM users WHERE email = $1',
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
      { sub: user.id, email: user.email, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(200).json({
      accessToken,
      user: {
        name: user.name,
        email: user.email,
        role: user.role,
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
      'SELECT id, name, email, role FROM users WHERE email = $1',
      [googleEmail]
    );

    let user;
    if (result.rows.length === 0) {
      // Primeiro acesso — cria o usuário sem senha
      const insert = await pool.query(
        'INSERT INTO users (name, email, password_hash, role) VALUES ($1, $2, $3, $4) RETURNING id, name, email, role',
        [googleName, googleEmail, '', 'USER']
      );
      user = insert.rows[0];
    } else {
      user = result.rows[0];
    }

    const accessToken = jwt.sign(
      { sub: user.id, email: user.email, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(200).json({
      accessToken,
      user: {
        name: user.name,
        email: user.email,
        role: user.role,
      },
    });

  } catch (err) {
    console.error('Erro no login Google:', err.message);
    return res.status(401).json({ message: 'Token Google inválido' });
  }
});

// ── Middleware de autenticação ──────────────────────────────────────────────
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Token de acesso requerido' });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ message: 'Token inválido' });
    }
    req.user = user;
    next();
  });
};

const requireAdmin = (req, res, next) => {
  if (req.user.role !== 'ADMIN') {
    return res.status(403).json({ message: 'Acesso negado. Apenas administradores.' });
  }
  next();
};

// ── GET /events ──────────────────────────────────────────────────────────────
app.get('/events', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        e.*,
        u.name as created_by_name,
        COUNT(er.id) as registered_count
      FROM events e
      LEFT JOIN users u ON e.created_by = u.id
      LEFT JOIN event_registrations er ON e.id = er.event_id
      GROUP BY e.id, u.name
      ORDER BY e.event_date ASC
    `);

    return res.json(result.rows);
  } catch (err) {
    console.error('Erro ao buscar eventos:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── POST /events ─────────────────────────────────────────────────────────────
app.post('/events', authenticateToken, requireAdmin, async (req, res) => {
  const { title, description, event_date, location, max_participants } = req.body;

  if (!title || !event_date || !location) {
    return res.status(400).json({ message: 'Título, data e local são obrigatórios' });
  }

  try {
    const result = await pool.query(
      'INSERT INTO events (title, description, event_date, location, max_participants, created_by) VALUES ($1, $2, $3, $4, $5, $6) RETURNING *',
      [title, description, event_date, location, max_participants, req.user.sub]
    );

    return res.status(201).json(result.rows[0]);
  } catch (err) {
    console.error('Erro ao criar evento:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── PUT /events/:id ──────────────────────────────────────────────────────────
app.put('/events/:id', authenticateToken, requireAdmin, async (req, res) => {
  const { id } = req.params;
  const { title, description, event_date, location, status, max_participants } = req.body;

  try {
    const result = await pool.query(
      'UPDATE events SET title = $1, description = $2, event_date = $3, location = $4, status = $5, max_participants = $6 WHERE id = $7 RETURNING *',
      [title, description, event_date, location, status, max_participants, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Evento não encontrado' });
    }

    return res.json(result.rows[0]);
  } catch (err) {
    console.error('Erro ao atualizar evento:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── DELETE /events/:id ───────────────────────────────────────────────────────
app.delete('/events/:id', authenticateToken, requireAdmin, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query('DELETE FROM events WHERE id = $1 RETURNING *', [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Evento não encontrado' });
    }

    return res.json({ message: 'Evento excluído com sucesso' });
  } catch (err) {
    console.error('Erro ao excluir evento:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── POST /events/:id/register ───────────────────────────────────────────────
app.post('/events/:id/register', authenticateToken, async (req, res) => {
  const { id } = req.params;
  const userId = req.user.sub;

  try {
    // Verifica se o evento existe e está aberto
    const eventResult = await pool.query(
      'SELECT * FROM events WHERE id = $1',
      [id]
    );

    if (eventResult.rows.length === 0) {
      return res.status(404).json({ message: 'Evento não encontrado' });
    }

    const event = eventResult.rows[0];
    if (event.status !== 'ABERTO') {
      return res.status(400).json({ message: 'Evento não está aberto para inscrições' });
    }

    // Verifica se já está inscrito
    const existingRegistration = await pool.query(
      'SELECT id FROM event_registrations WHERE user_id = $1 AND event_id = $2',
      [userId, id]
    );

    if (existingRegistration.rows.length > 0) {
      return res.status(400).json({ message: 'Você já está inscrito neste evento' });
    }

    // Verifica limite de participantes
    if (event.max_participants) {
      const registrationCount = await pool.query(
        'SELECT COUNT(*) as count FROM event_registrations WHERE event_id = $1',
        [id]
      );

      if (parseInt(registrationCount.rows[0].count) >= event.max_participants) {
        return res.status(400).json({ message: 'Evento lotado' });
      }
    }

    // Faz a inscrição
    await pool.query(
      'INSERT INTO event_registrations (user_id, event_id) VALUES ($1, $2)',
      [userId, id]
    );

    return res.json({ message: 'Inscrição realizada com sucesso' });
  } catch (err) {
    console.error('Erro ao se inscrever no evento:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /events/:id/is-registered ───────────────────────────────────────────
app.get('/events/:id/is-registered', authenticateToken, async (req, res) => {
  const { id } = req.params;
  const userId = req.user.sub;

  try {
    const result = await pool.query(
      'SELECT id FROM event_registrations WHERE user_id = $1 AND event_id = $2',
      [userId, id]
    );
    return res.json({ registered: result.rows.length > 0 });
  } catch (err) {
    console.error('Erro ao verificar inscrição:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /events/:id/registrations ───────────────────────────────────────────
app.get('/events/:id/registrations', authenticateToken, requireAdmin, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT 
        er.id,
        er.registered_at,
        u.name,
        u.email
      FROM event_registrations er
      JOIN users u ON er.user_id = u.id
      WHERE er.event_id = $1
      ORDER BY er.registered_at ASC
    `, [id]);

    return res.json(result.rows);
  } catch (err) {
    console.error('Erro ao buscar inscrições:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
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
