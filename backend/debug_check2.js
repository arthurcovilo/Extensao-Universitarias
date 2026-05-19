require('dotenv').config();
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');
const pool = new Pool({ connectionString: process.env.DATABASE_URL });

async function check() {
  // Check the old test users that work on the API
  const r = await pool.query(
    "SELECT email, password_hash, role FROM users WHERE email IN ('teste@email.com','admin@email.com') ORDER BY email"
  );

  console.log('\n--- Usuários antigos (que funcionam na API) ---');
  for (const row of r.rows) {
    const match = await bcrypt.compare('123456', row.password_hash);
    console.log((match ? '✅' : '❌'), row.email, '| role:', row.role, '| bcrypt OK:', match);
  }

  // Also show total user count
  const count = await pool.query('SELECT COUNT(*) as total FROM users');
  console.log('\nTotal de usuários no banco:', count.rows[0].total);

  await pool.end();
}

check().catch(console.error);
