package com.example.extensao;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VolunteerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_detail);

        // Recebe dados passados pela VoluntariosActivity
        String nome   = getIntent().getStringExtra("volunteer_name");
        String email  = getIntent().getStringExtra("volunteer_email");
        int eventos   = getIntent().getIntExtra("volunteer_events", 0);
        String areas  = getIntent().getStringExtra("volunteer_areas");
        String dias   = getIntent().getStringExtra("volunteer_days");

        TextView txtNome    = findViewById(R.id.txtNomeDetalhe);
        TextView txtEmail   = findViewById(R.id.txtEmailDetalhe);
        TextView txtEventos = findViewById(R.id.txtEventosDetalhe);
        TextView txtAreas   = findViewById(R.id.txtAreasDetalhe);
        TextView txtDias    = findViewById(R.id.txtDiasDetalhe);

        txtNome.setText(nome != null ? nome : "Voluntário");
        txtEmail.setText(email != null ? email : "—");
        txtEventos.setText(eventos == 1 ? "1 evento participado" : eventos + " eventos participados");
        txtAreas.setText((areas != null && !areas.isEmpty()) ? areas : "Não informado");
        txtDias.setText((dias != null && !dias.isEmpty()) ? dias : "Não informado");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }
}
