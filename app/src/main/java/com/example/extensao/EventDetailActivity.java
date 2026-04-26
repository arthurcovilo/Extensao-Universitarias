package com.example.extensao;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDetailActivity extends AppCompatActivity {

    TextView txtTituloDetalhe, txtStatusDetalhe, txtDataDetalhe, txtLocalDetalhe;
    TextView txtVagasDetalhe, txtDescricaoDetalhe, txtListaInscritos;
    Button btnInscreverDetalhe, btnEditarDetalhe, btnExcluirDetalhe;
    LinearLayout layoutInscritos;
    ProgressBar progressDetalhe;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int eventId;
    private Event evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        inicializarViews();

        eventId = getIntent().getIntExtra("event_id", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Erro ao carregar evento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarEvento();
        configurarBotoes();
    }

    private void inicializarViews() {
        txtTituloDetalhe = findViewById(R.id.txtTituloDetalhe);
        txtStatusDetalhe = findViewById(R.id.txtStatusDetalhe);
        txtDataDetalhe = findViewById(R.id.txtDataDetalhe);
        txtLocalDetalhe = findViewById(R.id.txtLocalDetalhe);
        txtVagasDetalhe = findViewById(R.id.txtVagasDetalhe);
        txtDescricaoDetalhe = findViewById(R.id.txtDescricaoDetalhe);
        txtListaInscritos = findViewById(R.id.txtListaInscritos);
        layoutInscritos = findViewById(R.id.layoutInscritos);
        btnInscreverDetalhe = findViewById(R.id.btnInscreverDetalhe);
        btnEditarDetalhe = findViewById(R.id.btnEditarDetalhe);
        btnExcluirDetalhe = findViewById(R.id.btnExcluirDetalhe);
        progressDetalhe = findViewById(R.id.progressDetalhe);
    }

    private void configurarBotoes() {
        btnInscreverDetalhe.setOnClickListener(v -> inscreverNoEvento());
        btnEditarDetalhe.setOnClickListener(v -> editarEvento());
        btnExcluirDetalhe.setOnClickListener(v -> confirmarExclusao());
    }

    private void carregarEvento() {
        setLoading(true);
        executor.execute(() -> {
            Event event = buscarEventoPorId(eventId);
            boolean jaInscrito = !sessionManager.isAdmin() &&
                    eventApiClient.isUserRegistered(eventId, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                setLoading(false);
                if (event != null) {
                    evento = event;
                    exibirEvento(event);
                    configurarVisibilidadeBotoes(event, jaInscrito);

                    if (sessionManager.isAdmin()) {
                        carregarInscritos();
                    }
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

    private void exibirEvento(Event event) {
        txtTituloDetalhe.setText(event.title);
        txtDescricaoDetalhe.setText(event.description.isEmpty() ? "Sem descrição" : event.description);
        txtLocalDetalhe.setText(event.location);

        // Formatar data
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtDataDetalhe.setText(outputFormat.format(inputFormat.parse(event.eventDate)));
        } catch (Exception e) {
            txtDataDetalhe.setText(event.eventDate);
        }

        // Status
        txtStatusDetalhe.setText(event.status);
        int statusColor;
        switch (event.status) {
            case "ABERTO":
                statusColor = getColor(android.R.color.holo_green_dark);
                break;
            case "ENCERRADO":
                statusColor = getColor(android.R.color.holo_orange_dark);
                break;
            case "CANCELADO":
                statusColor = getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = getColor(android.R.color.darker_gray);
        }
        txtStatusDetalhe.setBackgroundColor(statusColor);

        // Vagas
        if (event.maxParticipants > 0) {
            int vagasDisponiveis = event.maxParticipants - event.registeredCount;
            if (vagasDisponiveis > 0) {
                txtVagasDetalhe.setText(vagasDisponiveis + " vagas disponíveis de " + event.maxParticipants);
            } else {
                txtVagasDetalhe.setText("Evento lotado (" + event.maxParticipants + " inscritos)");
            }
        } else {
            txtVagasDetalhe.setText(event.registeredCount + " inscritos (sem limite)");
        }
    }

    private void configurarVisibilidadeBotoes(Event event, boolean jaInscrito) {
        if (sessionManager.isAdmin()) {
            btnEditarDetalhe.setVisibility(View.VISIBLE);
            btnExcluirDetalhe.setVisibility(View.VISIBLE);
            btnInscreverDetalhe.setVisibility(View.GONE);
        } else {
            btnEditarDetalhe.setVisibility(View.GONE);
            btnExcluirDetalhe.setVisibility(View.GONE);
            btnInscreverDetalhe.setVisibility(View.VISIBLE);

            if (jaInscrito) {
                marcarComoInscrito();
            } else if (event.isFull()) {
                btnInscreverDetalhe.setText("Evento Lotado");
                btnInscreverDetalhe.setEnabled(false);
            } else if (!event.isOpen()) {
                btnInscreverDetalhe.setText("Evento Encerrado");
                btnInscreverDetalhe.setEnabled(false);
            } else {
                btnInscreverDetalhe.setText("Inscrever-se");
                btnInscreverDetalhe.setEnabled(true);
            }
        }
    }

    private void marcarComoInscrito() {
        btnInscreverDetalhe.setText("Inscrito ✓");
        btnInscreverDetalhe.setEnabled(false);
        btnInscreverDetalhe.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFAAAAAA));
    }

    private void carregarInscritos() {
        layoutInscritos.setVisibility(View.VISIBLE);
        txtListaInscritos.setText("Carregando...");

        executor.execute(() -> {
            String inscritos = buscarInscritos(eventId);
            runOnUiThread(() -> {
                txtListaInscritos.setText(inscritos);
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

    private void inscreverNoEvento() {
        setLoading(true);
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.registerForEvent(eventId, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                setLoading(false);
                if (result.success) {
                    marcarComoInscrito();
                    Intent intent = new Intent(this, InscricaoSucessoActivity.class);
                    intent.putExtra("event_id", evento.id);
                    intent.putExtra("event_title", evento.title);
                    intent.putExtra("event_date", evento.eventDate);
                    intent.putExtra("event_location", evento.location);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void editarEvento() {
        Intent intent = new Intent(this, AdminEventActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
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
        progressDetalhe.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnInscreverDetalhe.setEnabled(!loading);
        btnEditarDetalhe.setEnabled(!loading);
        btnExcluirDetalhe.setEnabled(!loading);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega quando volta da tela de edição
        if (evento != null) {
            carregarEvento();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}