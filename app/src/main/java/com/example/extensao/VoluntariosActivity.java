package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoluntariosActivity extends AppCompatActivity implements VolunteerAdapter.OnVolunteerClickListener {

    // ── Listas fixas de opções ───────────────────────────────────────────────
    private static final List<String> AREAS_FIXAS = Arrays.asList(
            "Beleza", "Divulgação", "Logística", "Captação", "Educação", "Saúde", "Alimentação"
    );
    private static final List<String> DIAS_FIXOS = Arrays.asList(
            "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"
    );

    // ── Views ────────────────────────────────────────────────────────────────
    private RecyclerView recyclerVoluntarios;
    private VolunteerAdapter adapter;
    private TextView txtContador, txtVazio, btnLimparFiltros;
    private EditText editBusca;
    private ChipGroup chipGroupAreas, chipGroupDias, chipGroupAtivos;
    private HorizontalScrollView scrollFiltrosAtivos;

    // ── Estado ───────────────────────────────────────────────────────────────
    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<Volunteer> todosVoluntarios = new ArrayList<>();
    private final Set<String> filtrosAreasAtivos = new LinkedHashSet<>();
    private final Set<String> filtrosDiasAtivos  = new LinkedHashSet<>();
    private String filtroBusca = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voluntarios);

        sessionManager     = new SessionManager(this);
        volunteerApiClient = new VolunteerApiClient();

        recyclerVoluntarios  = findViewById(R.id.recyclerVoluntarios);
        txtContador          = findViewById(R.id.txtContadorVoluntarios);
        txtVazio             = findViewById(R.id.txtVazio);
        editBusca            = findViewById(R.id.editBuscaVoluntario);
        chipGroupAreas       = findViewById(R.id.chipGroupAreas);
        chipGroupDias        = findViewById(R.id.chipGroupDias);
        chipGroupAtivos      = findViewById(R.id.chipGroupAtivos);
        scrollFiltrosAtivos  = findViewById(R.id.scrollFiltrosAtivos);
        btnLimparFiltros     = findViewById(R.id.btnLimparFiltros);

        recyclerVoluntarios.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolunteerAdapter(new ArrayList<>(), this);
        recyclerVoluntarios.setAdapter(adapter);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        btnLimparFiltros.setOnClickListener(v -> limparTodosFiltros());

        configurarBusca();
        criarChipsAreas();
        criarChipsDias();
        carregarVoluntarios();
    }

    // ── Busca por nome ───────────────────────────────────────────────────────
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

    // ── Chips de área (lista fixa) ───────────────────────────────────────────
    private void criarChipsAreas() {
        chipGroupAreas.removeAllViews();
        for (String area : AREAS_FIXAS) {
            Chip chip = criarChip(area);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filtrosAreasAtivos.add(area);
                } else {
                    filtrosAreasAtivos.remove(area);
                }
                atualizarFiltrosAtivos();
                aplicarFiltros();
            });
            chipGroupAreas.addView(chip);
        }
    }

    // ── Chips de dia (lista fixa) ────────────────────────────────────────────
    private void criarChipsDias() {
        chipGroupDias.removeAllViews();
        for (String dia : DIAS_FIXOS) {
            Chip chip = criarChip(dia);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filtrosDiasAtivos.add(dia);
                } else {
                    filtrosDiasAtivos.remove(dia);
                }
                atualizarFiltrosAtivos();
                aplicarFiltros();
            });
            chipGroupDias.addView(chip);
        }
    }

    // ── Cria um chip padrão de filtro ────────────────────────────────────────
    private Chip criarChip(String label) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(false);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setCheckedIconVisible(true);
        chip.setTextSize(13f);
        return chip;
    }

    // ── Barra de filtros ativos ──────────────────────────────────────────────
    private void atualizarFiltrosAtivos() {
        chipGroupAtivos.removeAllViews();

        Set<String> todos = new LinkedHashSet<>();
        todos.addAll(filtrosAreasAtivos);
        todos.addAll(filtrosDiasAtivos);

        if (todos.isEmpty()) {
            scrollFiltrosAtivos.setVisibility(View.GONE);
            btnLimparFiltros.setVisibility(View.GONE);
            return;
        }

        scrollFiltrosAtivos.setVisibility(View.VISIBLE);
        btnLimparFiltros.setVisibility(View.VISIBLE);

        for (String filtro : todos) {
            Chip chip = new Chip(this);
            chip.setText(filtro);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setTextSize(12f);
            // Clique no X remove o filtro
            chip.setOnCloseIconClickListener(v -> removerFiltro(filtro));
            chipGroupAtivos.addView(chip);
        }
    }

    // ── Remove um filtro individual pelo X ──────────────────────────────────
    private void removerFiltro(String filtro) {
        boolean eraArea = filtrosAreasAtivos.remove(filtro);
        boolean eraDia  = filtrosDiasAtivos.remove(filtro);

        // Desmarca o chip correspondente no grupo correto
        ChipGroup grupo = eraArea ? chipGroupAreas : (eraDia ? chipGroupDias : null);
        if (grupo != null) {
            for (int i = 0; i < grupo.getChildCount(); i++) {
                View child = grupo.getChildAt(i);
                if (child instanceof Chip) {
                    Chip c = (Chip) child;
                    if (filtro.equals(c.getText().toString())) {
                        // Desabilita listener temporariamente para evitar loop
                        c.setOnCheckedChangeListener(null);
                        c.setChecked(false);
                        // Reanexa o listener correto
                        if (eraArea) {
                            c.setOnCheckedChangeListener((btn, checked) -> {
                                if (checked) filtrosAreasAtivos.add(filtro);
                                else filtrosAreasAtivos.remove(filtro);
                                atualizarFiltrosAtivos();
                                aplicarFiltros();
                            });
                        } else {
                            c.setOnCheckedChangeListener((btn, checked) -> {
                                if (checked) filtrosDiasAtivos.add(filtro);
                                else filtrosDiasAtivos.remove(filtro);
                                atualizarFiltrosAtivos();
                                aplicarFiltros();
                            });
                        }
                        break;
                    }
                }
            }
        }

        atualizarFiltrosAtivos();
        aplicarFiltros();
    }

    // ── Limpa todos os filtros ───────────────────────────────────────────────
    private void limparTodosFiltros() {
        filtrosAreasAtivos.clear();
        filtrosDiasAtivos.clear();

        // Desmarca todos os chips de área
        for (int i = 0; i < chipGroupAreas.getChildCount(); i++) {
            View child = chipGroupAreas.getChildAt(i);
            if (child instanceof Chip) {
                Chip c = (Chip) child;
                c.setOnCheckedChangeListener(null);
                c.setChecked(false);
            }
        }
        // Reanexa listeners de área
        criarChipsAreas();

        // Desmarca todos os chips de dia
        for (int i = 0; i < chipGroupDias.getChildCount(); i++) {
            View child = chipGroupDias.getChildAt(i);
            if (child instanceof Chip) {
                Chip c = (Chip) child;
                c.setOnCheckedChangeListener(null);
                c.setChecked(false);
            }
        }
        // Reanexa listeners de dia
        criarChipsDias();

        atualizarFiltrosAtivos();
        aplicarFiltros();
    }

    // ── Carrega voluntários da API ───────────────────────────────────────────
    private void carregarVoluntarios() {
        executor.execute(() -> {
            List<Volunteer> volunteers = volunteerApiClient.getVolunteers(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                todosVoluntarios = volunteers;
                aplicarFiltros();
            });
        });
    }

    // ── Aplica todos os filtros ativos ───────────────────────────────────────
    private void aplicarFiltros() {
        List<Volunteer> filtrados = new ArrayList<>();

        for (Volunteer v : todosVoluntarios) {
            // Filtro de busca por nome
            if (!filtroBusca.isEmpty() && !v.name.toLowerCase().contains(filtroBusca)) {
                continue;
            }
            // Filtro de áreas: voluntário deve ter PELO MENOS UMA das áreas selecionadas
            if (!filtrosAreasAtivos.isEmpty()) {
                boolean temAlgumaArea = false;
                if (v.areas != null) {
                    for (String area : filtrosAreasAtivos) {
                        if (v.areas.contains(area)) {
                            temAlgumaArea = true;
                            break;
                        }
                    }
                }
                if (!temAlgumaArea) continue;
            }
            // Filtro de dias: voluntário deve ter PELO MENOS UM dos dias selecionados
            if (!filtrosDiasAtivos.isEmpty()) {
                boolean temAlgumDia = false;
                if (v.availabilityDays != null) {
                    for (String dia : filtrosDiasAtivos) {
                        if (v.availabilityDays.contains(dia)) {
                            temAlgumDia = true;
                            break;
                        }
                    }
                }
                if (!temAlgumDia) continue;
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

    // ── Clique no card do voluntário ─────────────────────────────────────────
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
