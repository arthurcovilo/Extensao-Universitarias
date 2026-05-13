package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InscricaoSucessoActivity extends AppCompatActivity {

    TextView txtTituloEventoSucesso, txtDataEventoSucesso, txtLocalEventoSucesso, txtDataInscricaoSucesso;
    Button btnVerDetalhesEvento, btnVoltarEventos;

    private int eventId;
    private String eventTitle;
    private String eventDate;
    private String eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscricao_sucesso);

        inicializarViews();
        carregarDadosEvento();
        configurarBotoes();
        configurarBotaoVoltar();
    }

    private void inicializarViews() {
        txtTituloEventoSucesso = findViewById(R.id.txtTituloEventoSucesso);
        txtDataEventoSucesso = findViewById(R.id.txtDataEventoSucesso);
        txtLocalEventoSucesso = findViewById(R.id.txtLocalEventoSucesso);
        txtDataInscricaoSucesso = findViewById(R.id.txtDataInscricaoSucesso);
        btnVerDetalhesEvento = findViewById(R.id.btnVerDetalhesEvento);
        btnVoltarEventos = findViewById(R.id.btnVoltarEventos);
    }

    private void carregarDadosEvento() {
        eventId = getIntent().getIntExtra("event_id", -1);
        eventTitle = getIntent().getStringExtra("event_title");
        eventDate = getIntent().getStringExtra("event_date");
        eventLocation = getIntent().getStringExtra("event_location");

        txtTituloEventoSucesso.setText(eventTitle != null ? eventTitle : "Evento");

        if (eventDate != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(eventDate);
                txtDataEventoSucesso.setText(outputFormat.format(date));
            } catch (Exception e) {
                txtDataEventoSucesso.setText(eventDate);
            }
        }

        txtLocalEventoSucesso.setText(eventLocation != null ? eventLocation : "Local não informado");

        SimpleDateFormat inscricaoFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
        txtDataInscricaoSucesso.setText("Inscrito em: " + inscricaoFormat.format(new Date()));
    }

    private void configurarBotoes() {
        btnVerDetalhesEvento.setOnClickListener(v -> {
            if (eventId != -1) {
                Intent intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("event_id", eventId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        btnVoltarEventos.setOnClickListener(v -> voltarParaEventos());
    }

    /** Substitui onBackPressed() deprecated — compatível com Android 13+ */
    private void configurarBotaoVoltar() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                voltarParaEventos();
            }
        });
    }

    private void voltarParaEventos() {
        Intent intent = new Intent(this, EventosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
