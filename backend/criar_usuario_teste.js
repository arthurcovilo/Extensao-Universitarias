/**
 * Script para criar um usuário comum de teste.
 * Uso: node criar_usuario_teste.js
 */

require('dotenv').config();
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function criarUsuario() {
  const nome  = 'Usuário Teste';
  const email = 'teste@amoremmovimento.com';
  const senha = 'teste123';
  const role  = 'USER';

  const hash = await bcrypt.hash(senha, 10);

  try {
    const existing = await pool.query('SELECT id FROM users WHERE email = $1', [email]);
    if (existing.rows.length > 0) {
      console.log('⚠️  Usuário já existe:', email);
      return;
    }

    await pool.query(
      'INSERT INTO users (name, email, password_hash, role) VALUES ($1, $2, $3, $4)',
      [nome, email, hash, role]
    );

    console.log('✅ Usuário criado com sucesso!');
    console.log('   Email:', email);
    console.log('   Senha:', senha);
    console.log('   Role: ', role);
  } catch (err) {
    console.error('❌ Erro:', err.message);
  } finally {
    await pool.end();
  }
}

criarUsuario();
