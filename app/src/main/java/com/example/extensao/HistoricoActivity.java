package com.example.extensao;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorico;
    private HistoricoAdapter adapter;
    private TextView txtVazio;
    private TextView txtResumoInscritos, txtResumoParticipou, txtResumoCancelado, txtResumoNaoCompareceu;

    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        sessionManager     = new SessionManager(this);
        volunteerApiClient = new VolunteerApiClient();

        recyclerHistorico        = findViewById(R.id.recyclerHistorico);
        txtVazio                 = findViewById(R.id.txtHistoricoVazio);
        txtResumoInscritos       = findViewById(R.id.txtResumoInscritos);
        txtResumoParticipou      = findViewById(R.id.txtResumoParticipou);
        txtResumoCancelado       = findViewById(R.id.txtResumoCancelado);
        txtResumoNaoCompareceu   = findViewById(R.id.txtResumoNaoCompareceu);

        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricoAdapter(new ArrayList<>());
        recyclerHistorico.setAdapter(adapter);

        findViewById(R.id.btnVoltarHistorico).setOnClickListener(v -> finish());

        carregarHistorico();
    }

    private void carregarHistorico() {
        executor.execute(() -> {
            VolunteerApiClient.UserHistory history =
                    volunteerApiClient.getUserHistory(sessionManager.getAccessToken());

            runOnUiThread(() -> {
                txtResumoInscritos.setText(String.valueOf(history.totalInscritos));
                txtResumoParticipou.setText(String.valueOf(history.totalParticipou));
                txtResumoCancelado.setText(String.valueOf(history.totalCancelado));
                txtResumoNaoCompareceu.setText(String.valueOf(history.totalNaoCompareceu));

                if (history.historico.isEmpty()) {
                    txtVazio.setVisibility(View.VISIBLE);
                    recyclerHistorico.setVisibility(View.GONE);
                } else {
                    txtVazio.setVisibility(View.GONE);
                    recyclerHistorico.setVisibility(View.VISIBLE);
                    adapter.updateItems(history.historico);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
