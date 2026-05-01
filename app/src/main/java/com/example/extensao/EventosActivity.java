package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventosActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerEventos;
    FloatingActionButton fabAddEvent;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private EventAdapter eventAdapter;
    private List<Integer> registeredIds = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        recyclerEventos = findViewById(R.id.recyclerEventos);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);

        setupRecyclerView();
        setupFab();
        carregarEventos();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendario) {
                startActivity(new Intent(EventosActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_eventos) {
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(EventosActivity.this, PerfilActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(EventosActivity.this, ContatoActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupRecyclerView() {
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), sessionManager.isAdmin(), this);
        recyclerEventos.setAdapter(eventAdapter);
    }

    private void setupFab() {
        if (sessionManager.isAdmin()) {
            fabAddEvent.setVisibility(android.view.View.VISIBLE);
            fabAddEvent.setOnClickListener(v -> {
                Intent intent = new Intent(EventosActivity.this, AdminEventActivity.class);
                startActivity(intent);
            });
        }
    }

    private void carregarEventos() {
        executor.execute(() -> {
            List<Event> events = eventApiClient.getEvents();
            List<Integer> ids = sessionManager.isAdmin()
                    ? new ArrayList<>()
                    : eventApiClient.getUserRegisteredEventIds(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                registeredIds = new ArrayList<>(ids);
                eventAdapter.setRegisteredEventIds(registeredIds);
                eventAdapter.updateEvents(events);
                if (events.isEmpty()) {
                    Toast.makeText(this, "Nenhum evento encontrado", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onCancelClick(Event event, android.widget.Button btnInscrever) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancelar inscrição")
                .setMessage("Tem certeza que deseja cancelar sua inscrição em \"" + event.title + "\"?")
                .setPositiveButton("Sim, cancelar", (dialog, which) -> {
                    executor.execute(() -> {
                        EventApiClient.ApiResult result = eventApiClient.cancelRegistration(
                                event.id, sessionManager.getAccessToken());
                        runOnUiThread(() -> {
                            if (result.success) {
                                // Remove da lista local e atualiza botão
                                registeredIds.remove(Integer.valueOf(event.id));
                                eventAdapter.setRegisteredEventIds(registeredIds);
                                android.widget.Toast.makeText(this,
                                        "Inscrição cancelada com sucesso", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                android.widget.Toast.makeText(this,
                                        result.message, android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onRegisterClick(Event event, android.widget.Button btnInscrever) {
        // Abre o formulário de inscrição
        Intent intent = new Intent(EventosActivity.this, InscricaoFormActivity.class);
        intent.putExtra("event_id", event.id);
        intent.putExtra("event_title", event.title);
        intent.putExtra("event_date", event.eventDate);
        intent.putExtra("event_location", event.location);
        startActivity(intent);
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(EventosActivity.this, EventDetailActivity.class);
        intent.putExtra("event_id", event.id);
        startActivity(intent);
    }

    @Override
    public void onCardClick(Event event) {
        Intent intent = new Intent(EventosActivity.this, EventDetailActivity.class);
        intent.putExtra("event_id", event.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEventos(); // Recarrega quando volta para a tela
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}