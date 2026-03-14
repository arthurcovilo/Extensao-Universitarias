package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    TextView txtDataSelecionada;
    Button btnNovoEvento;
    BottomNavigationView bottomNavigationView;

    String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        txtDataSelecionada = findViewById(R.id.txtDataSelecionada);
        btnNovoEvento = findViewById(R.id.btnNovoEvento);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_calendario);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dataSelecionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtDataSelecionada.setText("Data selecionada: " + dataSelecionada);
        });

        btnNovoEvento.setOnClickListener(v -> {
            if (dataSelecionada.isEmpty()) {
                Toast.makeText(MainActivity.this, "Selecione uma data", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Novo evento em: " + dataSelecionada, Toast.LENGTH_SHORT).show();
            }
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