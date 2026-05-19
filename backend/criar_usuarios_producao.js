/**
 * Script para criar os usuários de produção no banco Neon.
 * Uso: node criar_usuarios_producao.js
 */

require('dotenv').config();
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

const usuarios = [
  {
    nome:  'Admin ONG 01',
    email: 'admin01@amoremmovimento.org.br',
    senha: 'Am0r@Mov!2026#A1',
    role:  'ADMIN',
  },
  {
    nome:  'Admin ONG 02',
    email: 'admin02@amoremmovimento.org.br',
    senha: 'Ong#Adm2026!B7m',
    role:  'ADMIN',
  },
  {
    nome:  'Admin ONG 03',
    email: 'admin03@amoremmovimento.org.br',
    senha: 'M0viment0@2026#C9',
    role:  'ADMIN',
  },
  {
    nome:  'Admin ONG 04',
    email: 'admin04@amoremmovimento.org.br',
    senha: 'S0lidar!Adm#2026D',
    role:  'ADMIN',
  },
  {
    nome:  'Admin ONG 05',
    email: 'admin05@amoremmovimento.org.br',
    senha: 'Am0r&Gestao#2026E',
    role:  'ADMIN',
  },
  {
    nome:  'Usuário Teste',
    email: 'teste@amoremmovimento.com',
    senha: 'teste123',
    role:  'USER',
  },
];

async function criarUsuarios() {
  for (const u of usuarios) {
    try {
      const existing = await pool.query('SELECT id, role FROM users WHERE email = $1', [u.email]);

      if (existing.rows.length > 0) {
        console.log(`⚠️  Já existe: ${u.email} (role: ${existing.rows[0].role}) — atualizando senha e role...`);
        const hash = await bcrypt.hash(u.senha, 10);
        await pool.query(
          'UPDATE users SET password_hash = $1, role = $2 WHERE email = $3',
          [hash, u.role, u.email]
        );
        console.log(`✅ Atualizado: ${u.email}`);
      } else {
        const hash = await bcrypt.hash(u.senha, 10);
        await pool.query(
          'INSERT INTO users (name, email, password_hash, role) VALUES ($1, $2, $3, $4)',
          [u.nome, u.email, hash, u.role]
        );
        console.log(`✅ Criado: ${u.email} / role: ${u.role}`);
      }
    } catch (err) {
      console.error(`❌ Erro com ${u.email}:`, err.message);
    }
  }

  await pool.end();
  console.log('\nPronto! Tente logar no app agora.');
}

criarUsuarios();
