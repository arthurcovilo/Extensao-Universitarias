/**
 * Cria a tabela `users` e insere um usuário de teste.
 * Execute uma vez: node setup_db.js
 */
require('dotenv').config();

const { Pool } = require('pg');
const bcrypt = require('bcryptjs');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function setup() {
  const client = await pool.connect();
  try {
    // Cria tabela se não existir
    await client.query(`
      CREATE TABLE IF NOT EXISTS users (
        id            SERIAL PRIMARY KEY,
        name          VARCHAR(255) NOT NULL,
        email         VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        created_at    TIMESTAMP DEFAULT NOW()
      );
    `);
    console.log('✅ Tabela users criada (ou já existia)');

    // Usuário de teste
    const testEmail = 'teste@email.com';
    const testPassword = '123456';
    const testName = 'Usuário Teste';

    const exists = await client.query('SELECT id FROM users WHERE email = $1', [testEmail]);
    if (exists.rows.length === 0) {
      const hash = await bcrypt.hash(testPassword, 10);
      await client.query(
        'INSERT INTO users (name, email, password_hash) VALUES ($1, $2, $3)',
        [testName, testEmail, hash]
      );
      console.log(`✅ Usuário de teste criado:`);
      console.log(`   Email: ${testEmail}`);
      console.log(`   Senha: ${testPassword}`);
    } else {
      console.log('ℹ️  Usuário de teste já existe, pulando inserção.');
    }

  } catch (err) {
    console.error('❌ Erro no setup:', err.message);
  } finally {
    client.release();
    await pool.end();
  }
}

setup();
