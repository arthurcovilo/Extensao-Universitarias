package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PerfilActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Button btnSair;
    TextView txtNomeUsuario, txtEmailUsuario, txtTipoUsuario;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnSair = findViewById(R.id.btnSair);
        txtNomeUsuario = findViewById(R.id.txtNotificacoes); // Reutilizando o ID existente
        txtEmailUsuario = findViewById(R.id.txtEmailUsuario);
        txtTipoUsuario = findViewById(R.id.txtTipoUsuario);

        bottomNavigationView.setSelectedItemId(R.id.nav_perfil);

        // Carrega dados do usuário
        carregarDadosUsuario();

        btnSair.setOnClickListener(v -> {
            sessionManager.clearSession();

            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendario) {
                startActivity(new Intent(PerfilActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_eventos) {
                startActivity(new Intent(PerfilActivity.this, EventosActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(PerfilActivity.this, ContatoActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void carregarDadosUsuario() {
        String nome = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole();

        txtNomeUsuario.setText(nome.isEmpty() ? "Usuário" : nome);
        txtEmailUsuario.setText(email.isEmpty() ? "email@exemplo.com" : email);
        
        String tipoUsuario = "ADMIN".equals(role) ? "Administrador" : "Voluntário";
        txtTipoUsuario.setText(tipoUsuario);
    }
}
