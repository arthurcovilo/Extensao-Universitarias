package com.example.extensao;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoluntariosActivity extends AppCompatActivity {

    private RecyclerView recyclerVoluntarios;
    private VolunteerAdapter adapter;
    private TextView txtContador, txtVazio;

    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voluntarios);

        sessionManager = new SessionManager(this);
        volunteerApiClient = new VolunteerApiClient();

        recyclerVoluntarios = findViewById(R.id.recyclerVoluntarios);
        txtContador = findViewById(R.id.txtContadorVoluntarios);
        txtVazio = findViewById(R.id.txtVazio);

        recyclerVoluntarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolunteerAdapter(new ArrayList<>());
        recyclerVoluntarios.setAdapter(adapter);

        carregarVoluntarios();
    }

    private void carregarVoluntarios() {
        executor.execute(() -> {
            List<Volunteer> volunteers = volunteerApiClient.getVolunteers(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                if (volunteers.isEmpty()) {
                    txtVazio.setVisibility(View.VISIBLE);
                    recyclerVoluntarios.setVisibility(View.GONE);
                    txtContador.setText("0 voluntários");
                } else {
                    txtVazio.setVisibility(View.GONE);
                    recyclerVoluntarios.setVisibility(View.VISIBLE);
                    adapter.updateVolunteers(volunteers);
                    String contador = volunteers.size() == 1
                            ? "1 voluntário"
                            : volunteers.size() + " voluntários";
                    txtContador.setText(contador);
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
