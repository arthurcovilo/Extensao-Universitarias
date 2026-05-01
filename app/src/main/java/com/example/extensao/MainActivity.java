package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements CalendarEventAdapter.OnEventClickListener {

    private TextView txtMesAno, txtDataSelecionada;
    private Button btnMesAnterior, btnProximoMes, btnNovoEvento;
    private RecyclerView recyclerCalendario, recyclerEventosCalendario;
    private BottomNavigationView bottomNavigationView;

    private SessionManager sessionManager;
    private EventApiClient eventApiClient;
    private CustomCalendarAdapter calendarAdapter;
    private CalendarEventAdapter calendarEventAdapter;
    private List<Event> todosEventos = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Mês/ano exibido atualmente
    private int currentYear;
    private int currentMonth; // 0-based

    // Data selecionada
    private int selectedYear = -1;
    private int selectedMonth = -1;
    private int selectedDay = -1;
    private String dataSelecionadaIso = "";
    private String dataSelecionadaDisplay = "";

    private static final String[] MESES = {
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        eventApiClient = new EventApiClient();

        inicializarViews();
        configurarNavegacao();

        // Inicia no mês atual
        Calendar hoje = Calendar.getInstance();
        currentYear = hoje.get(Calendar.YEAR);
        currentMonth = hoje.get(Calendar.MONTH);

        configurarCalendario();
        configurarListaEventos();
        carregarEventos();
    }

    private void inicializarViews() {
        txtMesAno = findViewById(R.id.txtMesAno);
        txtDataSelecionada = findViewById(R.id.txtDataSelecionada);
        btnMesAnterior = findViewById(R.id.btnMesAnterior);
        btnProximoMes = findViewById(R.id.btnProximoMes);
        btnNovoEvento = findViewById(R.id.btnNovoEvento);
        recyclerCalendario = findViewById(R.id.recyclerCalendario);
        recyclerEventosCalendario = findViewById(R.id.recyclerEventosCalendario);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Só admin vê o botão
        btnNovoEvento.setVisibility(sessionManager.isAdmin() ? View.VISIBLE : View.GONE);

        btnNovoEvento.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEventActivity.class);
            if (!dataSelecionadaIso.isEmpty()) {
                intent.putExtra("selected_date", dataSelecionadaIso);
            }
            startActivity(intent);
        });

        btnMesAnterior.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) { currentMonth = 11; currentYear--; }
            atualizarCalendario();
        });

        btnProximoMes.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) { currentMonth = 0; currentYear++; }
            atualizarCalendario();
        });
    }

    private void configurarCalendario() {
        calendarAdapter = new CustomCalendarAdapter(this, currentYear, currentMonth);
        calendarAdapter.setOnDayClickListener((year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            dataSelecionadaIso = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, day);
            dataSelecionadaDisplay = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d", day, month + 1, year);
            calendarAdapter.setSelectedDay(day);
            filtrarEventosPorData(dataSelecionadaIso);
        });

        recyclerCalendario.setLayoutManager(new GridLayoutManager(this, 7));
        recyclerCalendario.setAdapter(calendarAdapter);
        atualizarHeaderMes();
    }

    private void configurarListaEventos() {
        recyclerEventosCalendario.setLayoutManager(new LinearLayoutManager(this));
        calendarEventAdapter = new CalendarEventAdapter(new ArrayList<>(), this);
        recyclerEventosCalendario.setAdapter(calendarEventAdapter);
    }

    private void configurarNavegacao() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendario);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                startActivity(new Intent(this, FeedActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_calendario) return true;
            if (id == R.id.nav_eventos) {
                startActivity(new Intent(this, EventosActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_contato) {
                startActivity(new Intent(this, ContatoActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void atualizarCalendario() {
        // Recria o adapter para o novo mês
        calendarAdapter = new CustomCalendarAdapter(this, currentYear, currentMonth);
        calendarAdapter.setOnDayClickListener((year, month, day) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            dataSelecionadaIso = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, day);
            dataSelecionadaDisplay = String.format(Locale.getDefault(),
                    "%02d/%02d/%04d", day, month + 1, year);
            calendarAdapter.setSelectedDay(day);
            filtrarEventosPorData(dataSelecionadaIso);
        });

        // Restaura seleção se o dia selecionado pertence ao mês atual
        if (selectedYear == currentYear && selectedMonth == currentMonth) {
            calendarAdapter.setSelectedDay(selectedDay);
        }

        calendarAdapter.setEventsByDate(agruparEventosPorData(todosEventos));
        recyclerCalendario.setAdapter(calendarAdapter);
        atualizarHeaderMes();
    }

    private void atualizarHeaderMes() {
        txtMesAno.setText(MESES[currentMonth] + " " + currentYear);
    }

    private void carregarEventos() {
        executor.execute(() -> {
            todosEventos = eventApiClient.getEvents();
            runOnUiThread(() -> {
                Map<String, List<Event>> porData = agruparEventosPorData(todosEventos);
                calendarAdapter.setEventsByDate(porData);
                if (!dataSelecionadaIso.isEmpty()) {
                    filtrarEventosPorData(dataSelecionadaIso);
                }
            });
        });
    }

    // Agrupa eventos por data "yyyy-MM-dd"
    private Map<String, List<Event>> agruparEventosPorData(List<Event> eventos) {
        Map<String, List<Event>> map = new HashMap<>();
        for (Event e : eventos) {
            if (e.eventDate == null) continue;
            String key = e.eventDate.length() >= 10
                    ? e.eventDate.substring(0, 10) : e.eventDate;
            if (!map.containsKey(key)) map.put(key, new ArrayList<>());
            map.get(key).add(e);
        }
        return map;
    }

    private void filtrarEventosPorData(String dataIso) {
        List<Event> eventosDoDia = new ArrayList<>();
        for (Event e : todosEventos) {
            String dataEvento = e.eventDate != null && e.eventDate.length() >= 10
                    ? e.eventDate.substring(0, 10) : e.eventDate;
            if (dataIso.equals(dataEvento)) eventosDoDia.add(e);
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
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.id);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEventos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
