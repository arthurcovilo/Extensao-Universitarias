/**
 * Cria a tabela posts (feed de avisos) em um banco já existente,
 * sem rodar o setup_db.js de novo. Pode ser executado mais de uma vez.
 * Execute antes do deploy do servidor: node migrate_posts.js
 */
require('dotenv').config();

const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function migrate() {
  const client = await pool.connect();
  try {
    await client.query(`
      CREATE TABLE IF NOT EXISTS posts (
        id         SERIAL PRIMARY KEY,
        title      VARCHAR(255) NOT NULL,
        content    TEXT NOT NULL,
        author_id  INTEGER REFERENCES users(id) ON DELETE SET NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
      );
    `);
    await client.query(`CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts (created_at DESC);`);
    console.log('✅ Tabela posts criada/verificada');
  } catch (err) {
    console.error('❌ Erro na migração de posts:', err.message);
    process.exitCode = 1;
  } finally {
    client.release();
    await pool.end();
  }
}

migrate();
