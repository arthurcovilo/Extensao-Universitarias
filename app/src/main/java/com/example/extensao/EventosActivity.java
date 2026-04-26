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
                // TODO: Abrir tela de criação de evento
                Toast.makeText(this, "Criar evento (em desenvolvimento)", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void carregarEventos() {
        executor.execute(() -> {
            List<Event> events = eventApiClient.getEvents();
            runOnUiThread(() -> {
                eventAdapter.updateEvents(events);
                if (events.isEmpty()) {
                    Toast.makeText(this, "Nenhum evento encontrado", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onRegisterClick(Event event) {
        executor.execute(() -> {
            EventApiClient.ApiResult result = eventApiClient.registerForEvent(event.id, sessionManager.getAccessToken());
            runOnUiThread(() -> {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                if (result.success) {
                    // Recarrega eventos para atualizar contador
                    carregarEventos();
                }
            });
        });
    }

    @Override
    public void onEventClick(Event event) {
        // TODO: Abrir tela de detalhes/edição do evento para admin
        Toast.makeText(this, "Gerenciar evento: " + event.title + " (em desenvolvimento)", Toast.LENGTH_SHORT).show();
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