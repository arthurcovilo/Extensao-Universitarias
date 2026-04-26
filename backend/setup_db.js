/**
 * Cria as tabelas necessárias e insere dados de teste.
 * Execute uma vez: node setup_db.js
 */
require('dotenv').config();

const { Pool } = require('pg');
const bcrypt = require('bcryptjs');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function setup() {
  const client = await pool.connect();
  try {
    // ── Tabela users com campo role ──────────────────────────────────────────
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id            SERIAL PRIMARY KEY,
        name          VARCHAR(255) NOT NULL,
        email         VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        role          VARCHAR(10) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
        created_at    TIMESTAMP DEFAULT NOW()
      );
    `);
    
    // Adiciona coluna role se não existir (para bancos existentes)
    await client.query(`
      ALTER TABLE users 
      ADD COLUMN IF NOT EXISTS role VARCHAR(10) DEFAULT 'USER' 
      CHECK (role IN ('USER', 'ADMIN'));
    `);
    console.log('✅ Tabela users criada/atualizada');

    // ── Tabela events ────────────────────────────────────────────────────────
    await client.query(`
      CREATE TABLE IF NOT EXISTS events (
        id              SERIAL PRIMARY KEY,
        title           VARCHAR(255) NOT NULL,
        description     TEXT,
        event_date      DATE NOT NULL,
        location        VARCHAR(255) NOT NULL,
        status          VARCHAR(20) DEFAULT 'ABERTO' CHECK (status IN ('ABERTO', 'ENCERRADO', 'CANCELADO')),
        max_participants INTEGER,
        created_by      INTEGER REFERENCES users(id),
        created_at      TIMESTAMP DEFAULT NOW()
      );
    `);
    console.log('✅ Tabela events criada');

    // ── Tabela event_registrations ──────────────────────────────────────────
    await client.query(`
      CREATE TABLE IF NOT EXISTS event_registrations (
        id            SERIAL PRIMARY KEY,
        user_id       INTEGER REFERENCES users(id) ON DELETE CASCADE,
        event_id      INTEGER REFERENCES events(id) ON DELETE CASCADE,
        registered_at TIMESTAMP DEFAULT NOW(),
        UNIQUE(user_id, event_id)
      );
    `);
    console.log('✅ Tabela event_registrations criada');

    // ── Usuários de teste ───────────────────────────────────────────────────
    const testUsers = [
      { email: 'teste@email.com', password: '123456', name: 'Usuário Teste', role: 'USER' },
      { email: 'admin@email.com', password: '123456', name: 'Admin Teste', role: 'ADMIN' }
    ];

    for (const user of testUsers) {
      const exists = await client.query('SELECT id FROM users WHERE email = $1', [user.email]);
      if (exists.rows.length === 0) {
        const hash = await bcrypt.hash(user.password, 10);
        await client.query(
          'INSERT INTO users (name, email, password_hash, role) VALUES ($1, $2, $3, $4)',
          [user.name, user.email, hash, user.role]
        );
        console.log(`✅ ${user.role}: ${user.email} / ${user.password}`);
      }
    }

    // ── Eventos de teste ────────────────────────────────────────────────────
    const adminUser = await client.query('SELECT id FROM users WHERE role = $1 LIMIT 1', ['ADMIN']);
    if (adminUser.rows.length > 0) {
      const adminId = adminUser.rows[0].id;
      
      const testEvents = [
        {
          title: 'Distribuição de Alimentos',
          description: 'Ajude na distribuição de cestas básicas para famílias carentes.',
          date: '2026-05-15',
          location: 'Centro Comunitário - São Paulo',
          max_participants: 20
        },
        {
          title: 'Limpeza do Parque',
          description: 'Mutirão de limpeza e plantio de mudas no Parque da Cidade.',
          date: '2026-05-22',
          location: 'Parque da Cidade',
          max_participants: 30
        }
      ];

      for (const event of testEvents) {
        const exists = await client.query('SELECT id FROM events WHERE title = $1', [event.title]);
        if (exists.rows.length === 0) {
          await client.query(
            'INSERT INTO events (title, description, event_date, location, max_participants, created_by) VALUES ($1, $2, $3, $4, $5, $6)',
            [event.title, event.description, event.date, event.location, event.max_participants, adminId]
          );
          console.log(`✅ Evento criado: ${event.title}`);
        }
      }
    }

  } catch (err) {
    console.error('❌ Erro no setup:', err.message);
  } finally {
    client.release();
    await pool.end();
  }
}

setup();
