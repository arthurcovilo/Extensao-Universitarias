package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PerfilActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnSair = findViewById(R.id.btnSair);

        bottomNavigationView.setSelectedItemId(R.id.nav_perfil);

        btnSair.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(PerfilActivity.this);
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
}
