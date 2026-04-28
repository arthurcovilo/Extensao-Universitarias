package com.example.extensao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VolunteerDetailActivity extends AppCompatActivity {

    private TextView txtNome, txtEmail, txtEventos, txtHoras, txtAreas, txtDias;
    private TextView txtHistoricoVazio;
    private ProgressBar progressDetalhe;
    private RecyclerView recyclerHistorico;

    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_detail);

        sessionManager = new SessionManager(this);
        volunteerApiClient = new VolunteerApiClient();

        txtNome            = findViewById(R.id.txtDetalheNome);
        txtEmail           = findViewById(R.id.txtDetalheEmail);
        txtEventos         = findViewById(R.id.txtDetalheEventos);
        txtHoras           = findViewById(R.id.txtDetalheHoras);
        txtAreas           = findViewById(R.id.txtDetalheAreas);
        txtDias            = findViewById(R.id.txtDetalheDias);
        txtHistoricoVazio  = findViewById(R.id.txtHistoricoVazio);
        progressDetalhe    = findViewById(R.id.progressDetalheVoluntario);
        recyclerHistorico  = findViewById(R.id.recyclerHistorico);

        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorico.setNestedScrollingEnabled(false);

        findViewById(R.id.btnVoltarDetalhe).setOnClickListener(v -> finish());

        // Dados básicos passados pela tela anterior
        String nome   = getIntent().getStringExtra("volunteer_name");
        String email  = getIntent().getStringExtra("volunteer_email");
        int eventos   = getIntent().getIntExtra("volunteer_events", 0);
        int horas     = getIntent().getIntExtra("volunteer_hours", 0);
        String areas  = getIntent().getStringExtra("volunteer_areas");
        String dias   = getIntent().getStringExtra("volunteer_days");

        txtNome.setText(nome != null ? nome : "—");
        txtEmail.setText(email != null ? email : "—");
        txtEventos.setText(eventos == 1 ? "1 evento participado" : eventos + " eventos participados");
        txtHoras.setText(horas == 1 ? "1h voluntariada" : horas + "h voluntariadas");
        txtAreas.setText(areas != null && !areas.isEmpty() ? "Áreas: " + areas : "Áreas: não informado");
        txtDias.setText(dias != null && !dias.isEmpty() ? "Dias: " + dias : "Dias: não informado");

        if (email != null) {
            carregarHistorico(email);
        }
    }

    private void carregarHistorico(String email) {
        progressDetalhe.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            VolunteerApiClient.VolunteerHistory history =
                    volunteerApiClient.getVolunteerHistory(sessionManager.getAccessToken(), email);
            runOnUiThread(() -> {
                progressDetalhe.setVisibility(View.GONE);
                if (history.events.isEmpty()) {
                    txtHistoricoVazio.setVisibility(View.VISIBLE);
                } else {
                    txtHistoricoVazio.setVisibility(View.GONE);
                    recyclerHistorico.setAdapter(new HistoricoAdapter(history.events));
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    // ── Adapter interno para o histórico ────────────────────────────────────
    static class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

        private final List<Event> events;
        private final SimpleDateFormat inputFmt  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private final SimpleDateFormat outputFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        HistoricoAdapter(List<Event> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_historico_evento, parent, false);
            return new HistoricoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
            Event event = events.get(position);
            holder.txtTitulo.setText(event.title);
            holder.txtLocal.setText(event.location);
            holder.txtStatus.setText(event.status);

            // Cor do status
            int color;
            switch (event.status) {
                case "ABERTO":    color = 0xFF388E3C; break;
                case "ENCERRADO": color = 0xFFE65100; break;
                case "CANCELADO": color = 0xFFC62828; break;
                default:          color = 0xFF757575;
            }
            holder.txtStatus.setBackgroundColor(color);

            // Formatar data
            try {
                String dataFormatada = outputFmt.format(inputFmt.parse(
                        event.eventDate != null && event.eventDate.length() >= 10
                                ? event.eventDate.substring(0, 10)
                                : event.eventDate));
                holder.txtData.setText(dataFormatada);
            } catch (Exception e) {
                holder.txtData.setText(event.eventDate);
            }
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class HistoricoViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitulo, txtData, txtLocal, txtStatus;

            HistoricoViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitulo = itemView.findViewById(R.id.txtHistoricoTitulo);
                txtData   = itemView.findViewById(R.id.txtHistoricoData);
                txtLocal  = itemView.findViewById(R.id.txtHistoricoLocal);
                txtStatus = itemView.findViewById(R.id.txtHistoricoStatus);
            }
        }
    }
}
