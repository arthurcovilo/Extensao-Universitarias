package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    TextView txtDataSelecionada;
    Button btnNovoEvento;
    BottomNavigationView bottomNavigationView;

    private SessionManager sessionManager;

    // Data no formato yyyy-MM-dd para enviar ao AdminEventActivity
    private String dataSelecionadaIso = "";
    // Data no formato dd/MM/yyyy para exibir na tela
    private String dataSelecionadaDisplay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        calendarView = findViewById(R.id.calendarView);
        txtDataSelecionada = findViewById(R.id.txtDataSelecionada);
        btnNovoEvento = findViewById(R.id.btnNovoEvento);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_calendario);

        // Só exibe o botão "Novo Evento" para admins
        btnNovoEvento.setVisibility(sessionManager.isAdmin() ? View.VISIBLE : View.GONE);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dataSelecionadaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dataSelecionadaDisplay = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            txtDataSelecionada.setText("Data selecionada: " + dataSelecionadaDisplay);
        });

        btnNovoEvento.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminEventActivity.class);
            if (!dataSelecionadaIso.isEmpty()) {
                intent.putExtra("selected_date", dataSelecionadaIso);
            }
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendario) {
                return true;
            } else if (id == R.id.nav_eventos) {
                startActivity(new Intent(MainActivity.this, EventosActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(MainActivity.this, PerfilActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(MainActivity.this, ContatoActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }
}