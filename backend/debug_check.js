require('dotenv').config();
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');
const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function check() {
  // Check what is stored for the amoremmovimento users
  const r = await pool.query(
    "SELECT email, password_hash, role FROM users WHERE email LIKE '%amoremmovimento%' ORDER BY email"
  );

  console.log('\n--- Usuários no banco ---');
  for (const row of r.rows) {
    console.log('Email:', row.email, '| Role:', row.role, '| Hash:', row.password_hash.substring(0, 20) + '...');
  }

  // Test bcrypt compare directly for each user
  const credentials = [
    { email: 'admin01@amoremmovimento.org.br', senha: 'Am0r@Mov!2026#A1' },
    { email: 'admin02@amoremmovimento.org.br', senha: 'Ong#Adm2026!B7m' },
    { email: 'admin03@amoremmovimento.org.br', senha: 'M0viment0@2026#C9' },
    { email: 'admin04@amoremmovimento.org.br', senha: 'S0lidar!Adm#2026D' },
    { email: 'admin05@amoremmovimento.org.br', senha: 'Am0r&Gestao#2026E' },
    { email: 'teste@amoremmovimento.com',      senha: 'teste123' },
  ];

  console.log('\n--- Verificação bcrypt ---');
  for (const c of credentials) {
    const res = await pool.query('SELECT password_hash FROM users WHERE email = $1', [c.email]);
    if (res.rows.length > 0) {
      const match = await bcrypt.compare(c.senha, res.rows[0].password_hash);
      console.log((match ? '✅' : '❌'), c.email, '->', match ? 'SENHA OK' : 'SENHA ERRADA');
    } else {
      console.log('⚠️  Não encontrado:', c.email);
    }
  }

  await pool.end();
}

check().catch(console.error);
