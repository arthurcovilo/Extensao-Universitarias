require('dotenv').config();

const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Pool } = require('pg');
const { OAuth2Client } = require('google-auth-library');
const rateLimit = require('express-rate-limit');

const app = express();
app.use(express.json());

const WEB_CLIENT_ID = '487621614650-ekd8795v6uu6ac2uco886h95f09lrta4.apps.googleusercontent.com';
const googleClient = new OAuth2Client(WEB_CLIENT_ID);

// ── Rate limiting — protege contra força bruta no login ──────────────────────
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // janela de 15 minutos
  max: 10,                   // máximo 10 tentativas por IP na janela
  standardHeaders: true,
  legacyHeaders: false,
  message: { message: 'Muitas tentativas de login. Aguarde 15 minutos e tente novamente.' },
  skipSuccessfulRequests: true, // não conta tentativas bem-sucedidas
});

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
app.post('/auth/login', loginLimiter, async (req, res) => {
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
  const { title, description, event_date, location, max_participants, event_type } = req.body;

  if (!title || !event_date) {
    return res.status(400).json({ message: 'Título e data são obrigatórios' });
  }

  if (!event_type) {
    return res.status(400).json({ message: 'Tipo do evento é obrigatório' });
  }

  const validTypes = ['Presencial', 'Online', 'Retirada de Itens', 'Doação'];
  if (!validTypes.includes(event_type)) {
    return res.status(400).json({ message: 'Tipo de evento inválido' });
  }

  if (event_type !== 'Online' && !location) {
    return res.status(400).json({ message: 'Informe o local do evento' });
  }

  try {
    const result = await pool.query(
      'INSERT INTO events (title, description, event_date, location, max_participants, created_by, event_type) VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING *',
      [title, description, event_date, location, max_participants, req.user.sub, event_type]
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
  const { title, description, event_date, location, status, max_participants, event_type } = req.body;

  try {
    const result = await pool.query(
      'UPDATE events SET title = $1, description = $2, event_date = $3, location = $4, status = $5, max_participants = $6, event_type = $7 WHERE id = $8 RETURNING *',
      [title, description, event_date, location, status, max_participants, event_type, id]
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
  const { nome, telefone, primeiro_evento } = req.body;

  if (!nome || !nome.trim()) {
    return res.status(400).json({ message: 'Informe seu nome completo' });
  }
  if (!telefone || !telefone.trim()) {
    return res.status(400).json({ message: 'Informe seu telefone' });
  }

  try {
    const eventResult = await pool.query('SELECT * FROM events WHERE id = $1', [id]);
    if (eventResult.rows.length === 0) {
      return res.status(404).json({ message: 'Evento não encontrado' });
    }
    const event = eventResult.rows[0];
    if (event.status !== 'ABERTO') {
      return res.status(400).json({ message: 'Evento não está aberto para inscrições' });
    }

    const existingRegistration = await pool.query(
      'SELECT id FROM event_registrations WHERE user_id = $1 AND event_id = $2',
      [userId, id]
    );
    if (existingRegistration.rows.length > 0) {
      return res.status(400).json({ message: 'Você já está inscrito neste evento' });
    }

    if (event.max_participants) {
      const registrationCount = await pool.query(
        'SELECT COUNT(*) as count FROM event_registrations WHERE event_id = $1', [id]
      );
      if (parseInt(registrationCount.rows[0].count) >= event.max_participants) {
        return res.status(400).json({ message: 'Evento lotado' });
      }
    }

    await pool.query(
      'INSERT INTO event_registrations (user_id, event_id, nome, telefone, primeiro_evento) VALUES ($1, $2, $3, $4, $5)',
      [userId, id, nome.trim(), telefone.trim(), primeiro_evento === true || primeiro_evento === 'true']
    );

    return res.json({ message: 'Inscrição realizada com sucesso' });
  } catch (err) {
    console.error('Erro ao se inscrever no evento:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── DELETE /events/:id/register ─────────────────────────────────────────────
app.delete('/events/:id/register', authenticateToken, async (req, res) => {
  const { id } = req.params;
  const userId = req.user.sub;

  try {
    const result = await pool.query(
      'DELETE FROM event_registrations WHERE user_id = $1 AND event_id = $2 RETURNING id',
      [userId, id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Inscrição não encontrada' });
    }

    return res.json({ message: 'Inscrição cancelada com sucesso' });
  } catch (err) {
    console.error('Erro ao cancelar inscrição:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /user/registrations ─────────────────────────────────────────────────
app.get('/user/registrations', authenticateToken, async (req, res) => {
  const userId = req.user.sub;
  try {
    const result = await pool.query(
      'SELECT event_id FROM event_registrations WHERE user_id = $1',
      [userId]
    );
    const eventIds = result.rows.map(r => r.event_id);
    return res.json({ registeredEventIds: eventIds });
  } catch (err) {
    console.error('Erro ao buscar inscrições do usuário:', err.message);
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

// ── GET /events/:id/registrations/status ────────────────────────────────────
app.get('/events/:id/registrations/status', authenticateToken, requireAdmin, async (req, res) => {
  const { id } = req.params;

  try {
    const result = await pool.query(`
      SELECT
        u.id   AS user_id,
        u.name,
        u.email,
        COALESCE(er.participation_status, 'INSCRITO') AS participation_status
      FROM event_registrations er
      JOIN users u ON er.user_id = u.id
      WHERE er.event_id = $1
      ORDER BY u.name ASC
    `, [id]);

    return res.json(result.rows);
  } catch (err) {
    console.error('Erro ao buscar inscrições com status:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /volunteer/profile ──────────────────────────────────────────────
app.get('/volunteer/profile', authenticateToken, async (req, res) => {
  const userId = req.user.sub;

  try {
    const result = await pool.query(
      'SELECT areas, availability_days FROM volunteer_profiles WHERE user_id = $1',
      [userId]
    );

    if (result.rows.length === 0) {
      return res.json({ areas: [], availability_days: [] });
    }

    return res.json(result.rows[0]);
  } catch (err) {
    console.error('Erro ao buscar perfil de voluntário:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── PUT /volunteer/profile ──────────────────────────────────────────────
app.put('/volunteer/profile', authenticateToken, async (req, res) => {
  const userId = req.user.sub;
  const { areas, availability_days } = req.body;

  if (!Array.isArray(areas) || !Array.isArray(availability_days)) {
    return res.status(400).json({ message: 'Areas e availability_days devem ser arrays' });
  }

  try {
    await pool.query(`
      INSERT INTO volunteer_profiles (user_id, areas, availability_days, updated_at)
      VALUES ($1, $2, $3, NOW())
      ON CONFLICT (user_id)
      DO UPDATE SET 
        areas = EXCLUDED.areas,
        availability_days = EXCLUDED.availability_days,
        updated_at = NOW()
    `, [userId, areas, availability_days]);

    return res.json({ message: 'Perfil de voluntário salvo com sucesso' });
  } catch (err) {
    console.error('Erro ao salvar perfil de voluntário:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /volunteers ──────────────────────────────────────────────────────
app.get('/volunteers', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT 
        u.id,
        u.name,
        u.email,
        COALESCE(vp.areas, '{}') as areas,
        COALESCE(vp.availability_days, '{}') as availability_days,
        COUNT(er.id) as events_participated
      FROM users u
      LEFT JOIN volunteer_profiles vp ON u.id = vp.user_id
      LEFT JOIN event_registrations er ON u.id = er.user_id
      WHERE u.role = 'USER'
      GROUP BY u.id, u.name, u.email, vp.areas, vp.availability_days
      ORDER BY u.name ASC
    `);

    return res.json(result.rows);
  } catch (err) {
    console.error('Erro ao buscar voluntários:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /user/stats ──────────────────────────────────────────────────────
app.get('/user/stats', authenticateToken, async (req, res) => {
  const userId = req.user.sub;

  try {
    // Conta eventos participados
    const eventsResult = await pool.query(
      'SELECT COUNT(*) as count FROM event_registrations WHERE user_id = $1',
      [userId]
    );

    // Busca próximo evento inscrito
    const nextEventResult = await pool.query(`
      SELECT e.title, e.event_date
      FROM events e
      JOIN event_registrations er ON e.id = er.event_id
      WHERE er.user_id = $1 AND e.event_date >= CURRENT_DATE
      ORDER BY e.event_date ASC
      LIMIT 1
    `, [userId]);

    // Verifica se tem perfil de voluntário
    const profileResult = await pool.query(
      'SELECT areas, availability_days FROM volunteer_profiles WHERE user_id = $1',
      [userId]
    );

    const hasProfile = profileResult.rows.length > 0;
    const profile = hasProfile ? profileResult.rows[0] : { areas: [], availability_days: [] };
    
    // Calcula progresso do perfil em 3 critérios:
    // - Tem pelo menos 1 área selecionada: 34%
    // - Tem pelo menos 1 dia disponível: 33%
    // - Participou de pelo menos 1 evento: 33%
    const areasComplete = profile.areas && profile.areas.length > 0;
    const daysComplete = profile.availability_days && profile.availability_days.length > 0;
    const hasEvents = parseInt(eventsResult.rows[0].count) > 0;
    const profileProgress = (areasComplete ? 34 : 0) + (daysComplete ? 33 : 0) + (hasEvents ? 33 : 0);

    return res.json({
      events_participated: parseInt(eventsResult.rows[0].count),
      next_event: nextEventResult.rows.length > 0 ? nextEventResult.rows[0] : null,
      profile_progress: profileProgress
    });
  } catch (err) {
    console.error('Erro ao buscar estatísticas do usuário:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /user/history ────────────────────────────────────────────────────
app.get('/user/history', authenticateToken, async (req, res) => {
  const userId = req.user.sub;

  try {
    const result = await pool.query(`
      SELECT
        e.id          AS event_id,
        e.title,
        e.event_date,
        e.location,
        e.status      AS event_status,
        er.registered_at,
        COALESCE(er.participation_status, 'INSCRITO') AS participation_status
      FROM event_registrations er
      JOIN events e ON er.event_id = e.id
      WHERE er.user_id = $1
      ORDER BY e.event_date DESC
    `, [userId]);

    const rows = result.rows;
    const total_inscritos    = rows.length;
    const total_participou   = rows.filter(r => r.participation_status === 'PARTICIPOU').length;
    const total_cancelado    = rows.filter(r => r.participation_status === 'CANCELADO').length;
    const total_nao_compareceu = rows.filter(r => r.participation_status === 'NAO_COMPARECEU').length;

    return res.json({
      resumo: { total_inscritos, total_participou, total_cancelado, total_nao_compareceu },
      historico: rows
    });
  } catch (err) {
    console.error('Erro ao buscar histórico do usuário:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── PATCH /events/:id/registrations/:userId/status ───────────────────────
app.patch('/events/:id/registrations/:userId/status', authenticateToken, requireAdmin, async (req, res) => {
  const { id, userId } = req.params;
  const { participation_status } = req.body;

  const valid = ['INSCRITO', 'PARTICIPOU', 'CANCELADO', 'NAO_COMPARECEU'];
  if (!valid.includes(participation_status)) {
    return res.status(400).json({ message: 'Status inválido' });
  }

  try {
    const result = await pool.query(
      'UPDATE event_registrations SET participation_status = $1 WHERE event_id = $2 AND user_id = $3 RETURNING id',
      [participation_status, id, userId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ message: 'Inscrição não encontrada' });
    }

    return res.json({ message: 'Status atualizado com sucesso' });
  } catch (err) {
    console.error('Erro ao atualizar status:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /volunteers/:email/history ──────────────────────────────────────
app.get('/volunteers/:email/history', authenticateToken, requireAdmin, async (req, res) => {
  const { email } = req.params;

  try {
    const userResult = await pool.query(
      'SELECT id, name, email FROM users WHERE email = $1',
      [email.toLowerCase().trim()]
    );

    if (userResult.rows.length === 0) {
      return res.status(404).json({ message: 'Voluntário não encontrado' });
    }

    const user = userResult.rows[0];

    const eventsResult = await pool.query(`
      SELECT 
        e.id,
        e.title,
        e.event_date,
        e.location,
        e.status,
        er.registered_at
      FROM event_registrations er
      JOIN events e ON er.event_id = e.id
      WHERE er.user_id = $1
      ORDER BY e.event_date DESC
    `, [user.id]);

    return res.json({
      volunteer: {
        name: user.name,
        email: user.email
      },
      events: eventsResult.rows
    });
  } catch (err) {
    console.error('Erro ao buscar histórico do voluntário:', err.message);
    return res.status(500).json({ message: 'Erro interno do servidor' });
  }
});

// ── GET /admin/stats ─────────────────────────────────────────────────────
app.get('/admin/stats', authenticateToken, requireAdmin, async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT COUNT(*) as count FROM users WHERE role = $1',
      ['USER']
    );

    return res.json({
      total_volunteers: parseInt(result.rows[0].count)
    });
  } catch (err) {
    console.error('Erro ao buscar estatísticas do admin:', err.message);
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
