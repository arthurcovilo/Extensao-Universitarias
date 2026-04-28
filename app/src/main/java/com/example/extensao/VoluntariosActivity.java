package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoluntariosActivity extends AppCompatActivity implements VolunteerAdapter.OnVolunteerClickListener {

    private RecyclerView recyclerVoluntarios;
    private VolunteerAdapter adapter;
    private TextView txtContador, txtVazio;
    private EditText editBusca;
    private Spinner spinnerArea, spinnerDia;

    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Lista completa para filtrar localmente
    private List<Volunteer> todosVoluntarios = new ArrayList<>();

    // Filtros ativos
    private String filtroBusca = "";
    private String filtroArea  = "Todas as áreas";
    private String filtroDia   = "Todos os dias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voluntarios);

        sessionManager      = new SessionManager(this);
        volunteerApiClient  = new VolunteerApiClient();

        recyclerVoluntarios = findViewById(R.id.recyclerVoluntarios);
        txtContador         = findViewById(R.id.txtContadorVoluntarios);
        txtVazio            = findViewById(R.id.txtVazio);
        editBusca           = findViewById(R.id.editBuscaVoluntario);
        spinnerArea         = findViewById(R.id.spinnerFiltroArea);
        spinnerDia          = findViewById(R.id.spinnerFiltroDia);

        recyclerVoluntarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolunteerAdapter(new ArrayList<>(), this);
        recyclerVoluntarios.setAdapter(adapter);

        // Botão voltar
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        configurarBusca();
        configurarSpinnersVazios();
        carregarVoluntarios();
    }

    private void configurarBusca() {
        editBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filtroBusca = s.toString().trim().toLowerCase();
                aplicarFiltros();
            }
        });
    }

    private void configurarSpinnersVazios() {
        // Spinners iniciam com opção padrão enquanto dados não chegam
        setSpinnerOptions(spinnerArea, new ArrayList<String>() {{ add("Todas as áreas"); }}, area -> {
            filtroArea = area;
            aplicarFiltros();
        });
        setSpinnerOptions(spinnerDia, new ArrayList<String>() {{ add("Todos os dias"); }}, dia -> {
            filtroDia = dia;
            aplicarFiltros();
        });
    }

    private void configurarSpinnersComDados(List<Volunteer> volunteers) {
        // Coleta áreas únicas
        Set<String> areasSet = new LinkedHashSet<>();
        Set<String> diasSet  = new LinkedHashSet<>();
        for (Volunteer v : volunteers) {
            if (v.areas != null) areasSet.addAll(v.areas);
            if (v.availabilityDays != null) diasSet.addAll(v.availabilityDays);
        }

        List<String> areas = new ArrayList<>();
        areas.add("Todas as áreas");
        areas.addAll(areasSet);

        List<String> dias = new ArrayList<>();
        dias.add("Todos os dias");
        dias.addAll(diasSet);

        runOnUiThread(() -> {
            setSpinnerOptions(spinnerArea, areas, area -> {
                filtroArea = area;
                aplicarFiltros();
            });
            setSpinnerOptions(spinnerDia, dias, dia -> {
                filtroDia = dia;
                aplicarFiltros();
            });
        });
    }

    private void setSpinnerOptions(Spinner spinner, List<String> options, OnItemSelected callback) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.onSelected(options.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    interface OnItemSelected {
        void onSelected(String value);
    }

    private void carregarVoluntarios() {
        executor.execute(() -> {
            List<Volunteer> volunteers = volunteerApiClient.getVolunteers(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                todosVoluntarios = volunteers;
                configurarSpinnersComDados(volunteers);
                aplicarFiltros();
            });
        });
    }

    private void aplicarFiltros() {
        List<Volunteer> filtrados = new ArrayList<>();
        for (Volunteer v : todosVoluntarios) {
            // Filtro de busca por nome
            if (!filtroBusca.isEmpty() && !v.name.toLowerCase().contains(filtroBusca)) {
                continue;
            }
            // Filtro de área
            if (!"Todas as áreas".equals(filtroArea)) {
                if (v.areas == null || !v.areas.contains(filtroArea)) continue;
            }
            // Filtro de dia
            if (!"Todos os dias".equals(filtroDia)) {
                if (v.availabilityDays == null || !v.availabilityDays.contains(filtroDia)) continue;
            }
            filtrados.add(v);
        }

        if (filtrados.isEmpty()) {
            txtVazio.setVisibility(View.VISIBLE);
            recyclerVoluntarios.setVisibility(View.GONE);
            txtContador.setText("0 voluntários");
        } else {
            txtVazio.setVisibility(View.GONE);
            recyclerVoluntarios.setVisibility(View.VISIBLE);
            adapter.updateVolunteers(filtrados);
            String contador = filtrados.size() == 1
                    ? "1 voluntário"
                    : filtrados.size() + " voluntários";
            txtContador.setText(contador);
        }
    }

    @Override
    public void onVolunteerClick(Volunteer volunteer) {
        Intent intent = new Intent(this, VolunteerDetailActivity.class);
        intent.putExtra("volunteer_name",   volunteer.name);
        intent.putExtra("volunteer_email",  volunteer.email);
        intent.putExtra("volunteer_events", volunteer.eventsParticipated);
        intent.putExtra("volunteer_hours",  volunteer.totalHours);
        intent.putExtra("volunteer_areas",
                volunteer.areas != null ? String.join(", ", volunteer.areas) : "");
        intent.putExtra("volunteer_days",
                volunteer.availabilityDays != null ? String.join(", ", volunteer.availabilityDays) : "");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
