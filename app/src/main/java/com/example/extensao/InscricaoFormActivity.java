package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InscricaoFormActivity extends AppCompatActivity {

    private EditText editNome, editTelefone;
    private RadioGroup radioGroupPrimeiroEvento;
    private RadioButton radioSim;
    private Button btnConfirmar, btnCancelar;
    private ProgressBar progressForm;
    private TextView txtNomeEvento;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int eventId;
    private String eventTitle, eventDate, eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscricao_form);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        inicializarViews();
        carregarDadosEvento();
        configurarMascaraTelefone();
        configurarBotoes();
    }

    private void inicializarViews() {
        txtNomeEvento = findViewById(R.id.txtNomeEventoForm);
        editNome = findViewById(R.id.editNomeForm);
        editTelefone = findViewById(R.id.editTelefoneForm);
        radioGroupPrimeiroEvento = findViewById(R.id.radioGroupPrimeiroEvento);
        radioSim = findViewById(R.id.radioSim);
        btnConfirmar = findViewById(R.id.btnConfirmarInscricao);
        btnCancelar = findViewById(R.id.btnCancelarForm);
        progressForm = findViewById(R.id.progressForm);
    }

    private void carregarDadosEvento() {
        eventId = getIntent().getIntExtra("event_id", -1);
        eventTitle = getIntent().getStringExtra("event_title");
        eventDate = getIntent().getStringExtra("event_date");
        eventLocation = getIntent().getStringExtra("event_location");

        if (eventTitle != null) {
            txtNomeEvento.setText("📅 " + eventTitle);
        }

        // Pré-preenche o nome com o nome do usuário logado
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            editNome.setText(userName);
        }
    }

    private void configurarMascaraTelefone() {
        editTelefone.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            private String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                String formatted = "";

                if (str.length() > 11) str = str.substring(0, 11);

                if (str.length() >= 2) {
                    formatted = "(" + str.substring(0, 2) + ") ";
                    if (str.length() >= 7) {
                        formatted += str.substring(2, 7) + "-" + str.substring(7);
                    } else {
                        formatted += str.substring(2);
                    }
                } else {
                    formatted = str;
                }

                if (!formatted.equals(old)) {
                    editTelefone.setText(formatted);
                    editTelefone.setSelection(formatted.length());
                    old = formatted;
                }

                isUpdating = false;
            }
        });
    }

    private void configurarBotoes() {
        btnConfirmar.setOnClickListener(v -> validarEEnviar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void validarEEnviar() {
        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();
        boolean primeiroEvento = radioSim.isChecked();

        if (nome.isEmpty()) {
            editNome.setError("Informe seu nome completo");
            editNome.requestFocus();
            return;
        }

        // Remove máscara para validar tamanho mínimo
        String telefoneLimpo = telefone.replaceAll("[^\\d]", "");
        if (telefoneLimpo.isEmpty()) {
            editTelefone.setError("Informe seu telefone");
            editTelefone.requestFocus();
            return;
        }
        if (telefoneLimpo.length() < 10) {
            editTelefone.setError("Telefone inválido");
            editTelefone.requestFocus();
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.registerForEvent(
                    eventId, sessionManager.getAccessToken(), nome, telefone, primeiroEvento);
            runOnUiThread(() -> {
                setLoading(false);
                if (result.success) {
                    Intent intent = new Intent(this, InscricaoSucessoActivity.class);
                    intent.putExtra("event_id", eventId);
                    intent.putExtra("event_title", eventTitle);
                    intent.putExtra("event_date", eventDate);
                    intent.putExtra("event_location", eventLocation);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        progressForm.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnConfirmar.setEnabled(!loading);
        btnCancelar.setEnabled(!loading);
        editNome.setEnabled(!loading);
        editTelefone.setEnabled(!loading);
        radioGroupPrimeiroEvento.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
