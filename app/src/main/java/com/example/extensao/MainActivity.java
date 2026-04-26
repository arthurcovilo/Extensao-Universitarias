package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements CalendarEventAdapter.OnEventClickListener {

    CalendarView calendarView;
    TextView txtDataSelecionada;
    Button btnNovoEvento;
    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerEventosCalendario;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private CalendarEventAdapter calendarEventAdapter;
    private List<Event> todosEventos = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Data no formato yyyy-MM-dd para enviar ao AdminEventActivity e filtrar
    private String dataSelecionadaIso = "";
    // Data no formato dd/MM/yyyy para exibir na tela
    private String dataSelecionadaDisplay = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        calendarView = findViewById(R.id.calendarView);
        txtDataSelecionada = findViewById(R.id.txtDataSelecionada);
        btnNovoEvento = findViewById(R.id.btnNovoEvento);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        recyclerEventosCalendario = findViewById(R.id.recyclerEventosCalendario);

        bottomNavigationView.setSelectedItemId(R.id.nav_calendario);

        // Só exibe o botão "Novo Evento" para admins
        btnNovoEvento.setVisibility(sessionManager.isAdmin() ? View.VISIBLE : View.GONE);

        // Configura RecyclerView
        recyclerEventosCalendario.setLayoutManager(new LinearLayoutManager(this));
        calendarEventAdapter = new CalendarEventAdapter(new ArrayList<>(), this);
        recyclerEventosCalendario.setAdapter(calendarEventAdapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            dataSelecionadaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dataSelecionadaDisplay = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            txtDataSelecionada.setText("Eventos em " + dataSelecionadaDisplay + ":");
            filtrarEventosPorData(dataSelecionadaIso);
        });

        btnNovoEvento.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminEventActivity.class);
            if (!dataSelecionadaIso.isEmpty()) {
                intent.putExtra("selected_date", dataSelecionadaIso);
            }
            startActivity(intent);
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

        carregarTodosEventos();
    }

    private void carregarTodosEventos() {
        executor.execute(() -> {
            todosEventos = eventApiClient.getEvents();
            runOnUiThread(() -> {
                // Se já tem uma data selecionada, filtra imediatamente
                if (!dataSelecionadaIso.isEmpty()) {
                    filtrarEventosPorData(dataSelecionadaIso);
                }
            });
        });
    }

    // Filtra eventos comparando apenas yyyy-MM-dd (ignora horário)
    private void filtrarEventosPorData(String dataIso) {
        List<Event> eventosDoDia = new ArrayList<>();
        for (Event event : todosEventos) {
            // event.eventDate vem como "yyyy-MM-dd" ou "yyyy-MM-ddTHH:mm:ss..."
            String dataEvento = event.eventDate != null && event.eventDate.length() >= 10
                    ? event.eventDate.substring(0, 10)
                    : event.eventDate;
            if (dataIso.equals(dataEvento)) {
                eventosDoDia.add(event);
            }
        }

        calendarEventAdapter.updateEvents(eventosDoDia);

        if (eventosDoDia.isEmpty()) {
            txtDataSelecionada.setText("Nenhum evento em " + dataSelecionadaDisplay);
        } else {
            txtDataSelecionada.setText("Eventos em " + dataSelecionadaDisplay + ":");
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
        intent.putExtra("event_id", event.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega eventos ao voltar (ex: após criar um novo)
        carregarTodosEventos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
