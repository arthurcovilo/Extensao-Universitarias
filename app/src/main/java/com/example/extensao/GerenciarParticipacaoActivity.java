package com.example.extensao;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GerenciarParticipacaoActivity extends AppCompatActivity {

    private static final String BASE_URL = AppConfig.BASE_URL;

    private RecyclerView recyclerInscritos;
    private InscritosParticipacaoAdapter adapter;
    private TextView txtVazio, txtNomeEvento;

    private SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_participacao);

        sessionManager = new SessionManager(this);

        recyclerInscritos = findViewById(R.id.recyclerInscritos);
        txtVazio          = findViewById(R.id.txtGerenciarVazio);
        txtNomeEvento     = findViewById(R.id.txtNomeEventoGerenciar);

        eventId = getIntent().getIntExtra("event_id", -1);
        String nomeEvento = getIntent().getStringExtra("event_title");

        if (eventId == -1) {
            Toast.makeText(this, "Erro ao carregar evento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (nomeEvento != null) txtNomeEvento.setText(nomeEvento);

        recyclerInscritos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InscritosParticipacaoAdapter(new ArrayList<>(), this::atualizarStatus);
        recyclerInscritos.setAdapter(adapter);

        findViewById(R.id.btnVoltarGerenciar).setOnClickListener(v -> finish());

        carregarInscritos();
    }

    private void carregarInscritos() {
        executor.execute(() -> {
            List<InscritoParticipacao> inscritos = buscarInscritosComStatus();
            runOnUiThread(() -> {
                if (inscritos.isEmpty()) {
                    txtVazio.setVisibility(View.VISIBLE);
                    recyclerInscritos.setVisibility(View.GONE);
                } else {
                    txtVazio.setVisibility(View.GONE);
                    recyclerInscritos.setVisibility(View.VISIBLE);
                    adapter.updateInscritos(inscritos);
                }
            });
        });
    }

    private List<InscritoParticipacao> buscarInscritosComStatus() {
        List<InscritoParticipacao> lista = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/events/" + eventId + "/registrations/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + sessionManager.getAccessToken());
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray array = new JSONArray(sb.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    InscritoParticipacao inscrito = new InscritoParticipacao();
                    inscrito.userId              = obj.optInt("user_id");
                    inscrito.name                = obj.optString("name");
                    inscrito.email               = obj.optString("email");
                    inscrito.participationStatus = obj.optString("participation_status", "INSCRITO");
                    lista.add(inscrito);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e("GerenciarParticipacao", "Erro ao buscar inscritos", e);
        }
        return lista;
    }

    private void atualizarStatus(InscritoParticipacao inscrito, String novoStatus) {
        executor.execute(() -> {
            boolean sucesso = enviarStatus(inscrito.userId, novoStatus);
            runOnUiThread(() -> {
                if (!sucesso) {
                    Toast.makeText(this, "Erro ao salvar status", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean enviarStatus(int userId, String status) {
        try {
            URL url = new URL(BASE_URL + "/events/" + eventId + "/registrations/" + userId + "/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + sessionManager.getAccessToken());
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            JSONObject body = new JSONObject();
            body.put("participation_status", status);

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            conn.disconnect();
            return code == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.e("GerenciarParticipacao", "Erro ao atualizar status", e);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
