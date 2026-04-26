package com.example.extensao;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminEventActivity extends AppCompatActivity {

    TextView txtTituloAdmin, txtStatusLabel;
    EditText editTitulo, editDescricao, editLocal, editLimite;
    Button btnSelecionarData, btnSalvar, btnExcluir, btnVerInscritos;
    Spinner spinnerStatus;
    ProgressBar progressAdmin;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String dataSelecionada = "";
    private int eventId = -1; // -1 = criar novo, >0 = editar existente
    private Event eventoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        // Verifica se é admin
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarViews();
        configurarSpinnerStatus();
        configurarBotoes();

        // Verifica se está editando um evento existente
        if (getIntent().hasExtra("event_id")) {
            eventId = getIntent().getIntExtra("event_id", -1);
            carregarEvento(eventId);
        } else if (getIntent().hasExtra("selected_date")) {
            // Pré-preenche a data vinda do calendário
            String selectedDate = getIntent().getStringExtra("selected_date");
            preencherDataSelecionada(selectedDate);
        }
    }

    private void inicializarViews() {
        txtTituloAdmin = findViewById(R.id.txtTituloAdmin);
        txtStatusLabel = findViewById(R.id.txtStatusLabel);
        editTitulo = findViewById(R.id.editTitulo);
        editDescricao = findViewById(R.id.editDescricao);
        editLocal = findViewById(R.id.editLocal);
        editLimite = findViewById(R.id.editLimite);
        btnSelecionarData = findViewById(R.id.btnSelecionarData);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnExcluir = findViewById(R.id.btnExcluir);
        btnVerInscritos = findViewById(R.id.btnVerInscritos);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        progressAdmin = findViewById(R.id.progressAdmin);
    }

    private void configurarSpinnerStatus() {
        String[] statusOptions = {"ABERTO", "ENCERRADO", "CANCELADO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void configurarBotoes() {
        btnSelecionarData.setOnClickListener(v -> mostrarDatePicker());
        btnSalvar.setOnClickListener(v -> salvarEvento());
        btnExcluir.setOnClickListener(v -> confirmarExclusao());
        btnVerInscritos.setOnClickListener(v -> verInscritos());
    }

    private void preencherDataSelecionada(String dataIso) {
        dataSelecionada = dataIso;
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnSelecionarData.setText(displayFormat.format(parseFormat.parse(dataIso)));
        } catch (Exception e) {
            btnSelecionarData.setText(dataIso);
        }
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dataSelecionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        btnSelecionarData.setText(displayFormat.format(parseFormat.parse(dataSelecionada)));
                    } catch (Exception e) {
                        btnSelecionarData.setText(dataSelecionada);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void salvarEvento() {
        String titulo = editTitulo.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String local = editLocal.getText().toString().trim();
        String limiteStr = editLimite.getText().toString().trim();

        if (titulo.isEmpty()) {
            editTitulo.setError("Título é obrigatório");
            editTitulo.requestFocus();
            return;
        }

        if (dataSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (local.isEmpty()) {
            editLocal.setError("Local é obrigatório");
            editLocal.requestFocus();
            return;
        }

        int limite = 0;
        if (!limiteStr.isEmpty()) {
            try {
                limite = Integer.parseInt(limiteStr);
            } catch (NumberFormatException e) {
                editLimite.setError("Número inválido");
                editLimite.requestFocus();
                return;
            }
        }

        Event event = new Event(titulo, descricao, dataSelecionada, local, limite);

        if (eventId > 0) {
            // Editar evento existente
            event.id = eventId;
            event.status = spinnerStatus.getSelectedItem().toString();
            atualizarEvento(event);
        } else {
            // Criar novo evento
            criarEvento(event);
        }
    }

    private void criarEvento(Event event) {
        setLoading(true);
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.createEvent(event, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                if (result.success) {
                    finish();
                }
            });
        });
    }

    private void atualizarEvento(Event event) {
        setLoading(true);
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.updateEvent(event, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                if (result.success) {
                    finish();
                }
            });
        });
    }

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Evento")
                .setMessage("Tem certeza que deseja excluir este evento? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> excluirEvento())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirEvento() {
        setLoading(true);
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.deleteEvent(eventId, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                setLoading(false);
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                if (result.success) {
                    finish();
                }
            });
        });
    }

    private void carregarEvento(int id) {
        txtTituloAdmin.setText("Editar Evento");
        txtStatusLabel.setVisibility(View.VISIBLE);
        spinnerStatus.setVisibility(View.VISIBLE);
        btnExcluir.setVisibility(View.VISIBLE);
        btnVerInscritos.setVisibility(View.VISIBLE);

        setLoading(true);
        executor.execute(() -> {
            Event event = buscarEventoPorId(id);
            runOnUiThread(() -> {
                setLoading(false);
                if (event != null) {
                    eventoAtual = event;
                    preencherFormulario(event);
                } else {
                    Toast.makeText(this, "Erro ao carregar evento", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private Event buscarEventoPorId(int id) {
        try {
            URL url = new URL("http://10.0.2.2:8080/events");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readBody(connection.getInputStream());
                JSONArray jsonArray = new JSONArray(responseBody);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject eventJson = jsonArray.getJSONObject(i);
                    if (eventJson.getInt("id") == id) {
                        return Event.fromJson(eventJson);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void preencherFormulario(Event event) {
        editTitulo.setText(event.title);
        editDescricao.setText(event.description);
        editLocal.setText(event.location);
        editLimite.setText(event.maxParticipants > 0 ? String.valueOf(event.maxParticipants) : "0");
        
        dataSelecionada = event.eventDate;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            btnSelecionarData.setText(outputFormat.format(inputFormat.parse(event.eventDate)));
        } catch (Exception e) {
            btnSelecionarData.setText(event.eventDate);
        }

        // Seleciona status no spinner
        String[] statusOptions = {"ABERTO", "ENCERRADO", "CANCELADO"};
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(event.status)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
    }

    private void verInscritos() {
        setLoading(true);
        executor.execute(() -> {
            String inscritos = buscarInscritos(eventId);
            runOnUiThread(() -> {
                setLoading(false);
                mostrarDialogInscritos(inscritos);
            });
        });
    }

    private String buscarInscritos(int eventId) {
        try {
            URL url = new URL("http://10.0.2.2:8080/events/" + eventId + "/registrations");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + sessionManager.getAccessToken());

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readBody(connection.getInputStream());
                JSONArray jsonArray = new JSONArray(responseBody);

                if (jsonArray.length() == 0) {
                    return "Nenhum inscrito ainda.";
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject inscrito = jsonArray.getJSONObject(i);
                    sb.append((i + 1)).append(". ")
                            .append(inscrito.getString("name"))
                            .append("\n   ")
                            .append(inscrito.getString("email"))
                            .append("\n\n");
                }
                return sb.toString();
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Erro ao carregar inscritos.";
    }

    private void mostrarDialogInscritos(String inscritos) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_inscritos, null);
        TextView txtListaInscritos = dialogView.findViewById(R.id.txtListaInscritos);
        txtListaInscritos.setText(inscritos);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private String readBody(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private void setLoading(boolean loading) {
        progressAdmin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSalvar.setEnabled(!loading);
        btnSelecionarData.setEnabled(!loading);
        editTitulo.setEnabled(!loading);
        editDescricao.setEnabled(!loading);
        editLocal.setEnabled(!loading);
        editLimite.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}