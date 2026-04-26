package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InscricaoConfirmadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscricao_confirmada);

        TextView txtNome = findViewById(R.id.txtNomeEvento);
        TextView txtData = findViewById(R.id.txtDataEvento);
        TextView txtLocal = findViewById(R.id.txtLocalEvento);
        Button btnVoltar = findViewById(R.id.btnVoltar);
        Button btnVerCalendario = findViewById(R.id.btnVerCalendario);

        // Recebe dados do evento via Intent
        String nome = getIntent().getStringExtra("event_title");
        String data = getIntent().getStringExtra("event_date");
        String local = getIntent().getStringExtra("event_location");

        txtNome.setText(nome != null ? nome : "");
        txtLocal.setText(local != null ? local : "");

        // Formata data de yyyy-MM-dd para dd/MM/yyyy
        if (data != null) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                txtData.setText(output.format(input.parse(data)));
            } catch (Exception e) {
                txtData.setText(data);
            }
        }

        // Volta para a tela de eventos e limpa o back stack até ela
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventosActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnVerCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Impede voltar com o botão físico para a tela de inscrição
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, EventosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
