package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerfilActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Button btnSair, btnSalvarPerfil, btnVerVoluntarios;
    TextView txtNomeUsuario, txtEmailUsuario, txtTipoUsuario;
    TextView txtEventosParticipados, txtProgressoPerfil, txtProximoEvento, txtTotalVoluntarios;
    
    LinearLayout layoutEngajamento, layoutPerfilVoluntario, layoutAdmin;
    
    CheckBox checkBeleza, checkLogistica, checkDivulgacao, checkCaptacao;
    CheckBox checkSegunda, checkTerca, checkQuarta, checkQuinta, checkSexta, checkSabado, checkDomingo;

    private SessionManager sessionManager;
    private VolunteerApiClient volunteerApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        sessionManager = new SessionManager(this);
        volunteerApiClient = new VolunteerApiClient();

        initializeViews();
        setupBottomNavigation();
        carregarDadosUsuario();
        
        if (sessionManager.isAdmin()) {
            mostrarSecaoAdmin();
        } else {
            mostrarSecaoVoluntario();
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        btnSair = findViewById(R.id.btnSair);
        btnSalvarPerfil = findViewById(R.id.btnSalvarPerfil);
        btnVerVoluntarios = findViewById(R.id.btnVerVoluntarios);
        
        txtNomeUsuario = findViewById(R.id.txtNotificacoes);
        txtEmailUsuario = findViewById(R.id.txtEmailUsuario);
        txtTipoUsuario = findViewById(R.id.txtTipoUsuario);
        txtEventosParticipados = findViewById(R.id.txtEventosParticipados);
        txtProgressoPerfil = findViewById(R.id.txtProgressoPerfil);
        txtProximoEvento = findViewById(R.id.txtProximoEvento);
        txtTotalVoluntarios = findViewById(R.id.txtTotalVoluntarios);
        
        layoutEngajamento = findViewById(R.id.layoutEngajamento);
        layoutPerfilVoluntario = findViewById(R.id.layoutPerfilVoluntario);
        layoutAdmin = findViewById(R.id.layoutAdmin);
        
        checkBeleza = findViewById(R.id.checkBeleza);
        checkLogistica = findViewById(R.id.checkLogistica);
        checkDivulgacao = findViewById(R.id.checkDivulgacao);
        checkCaptacao = findViewById(R.id.checkCaptacao);
        
        checkSegunda = findViewById(R.id.checkSegunda);
        checkTerca = findViewById(R.id.checkTerca);
        checkQuarta = findViewById(R.id.checkQuarta);
        checkQuinta = findViewById(R.id.checkQuinta);
        checkSexta = findViewById(R.id.checkSexta);
        checkSabado = findViewById(R.id.checkSabado);
        checkDomingo = findViewById(R.id.checkDomingo);

        btnSair.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSalvarPerfil.setOnClickListener(v -> salvarPerfilVoluntario());
        btnVerVoluntarios.setOnClickListener(v -> {
            startActivity(new Intent(PerfilActivity.this, VoluntariosActivity.class));
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_perfil);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendario) {
                startActivity(new Intent(PerfilActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_eventos) {
                startActivity(new Intent(PerfilActivity.this, EventosActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(PerfilActivity.this, ContatoActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void carregarDadosUsuario() {
        String nome = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole();

        txtNomeUsuario.setText(nome.isEmpty() ? "Usuário" : nome);
        txtEmailUsuario.setText(email.isEmpty() ? "email@exemplo.com" : email);
        
        String tipoUsuario = "ADMIN".equals(role) ? "Administrador" : "Voluntário";
        txtTipoUsuario.setText(tipoUsuario);
    }

    private void mostrarSecaoAdmin() {
        layoutEngajamento.setVisibility(View.GONE);
        layoutPerfilVoluntario.setVisibility(View.GONE);
        layoutAdmin.setVisibility(View.VISIBLE);
        
        carregarEstatisticasAdmin();
    }

    private void mostrarSecaoVoluntario() {
        layoutEngajamento.setVisibility(View.VISIBLE);
        layoutPerfilVoluntario.setVisibility(View.VISIBLE);
        layoutAdmin.setVisibility(View.GONE);
        
        carregarEstatisticasUsuario();
        carregarPerfilVoluntario();
    }

    private void carregarEstatisticasAdmin() {
        executor.execute(() -> {
            VolunteerApiClient.AdminStats stats = volunteerApiClient.getAdminStats(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                txtTotalVoluntarios.setText(stats.totalVolunteers + " voluntários cadastrados");
            });
        });
    }

    private void carregarEstatisticasUsuario() {
        executor.execute(() -> {
            VolunteerApiClient.UserStats stats = volunteerApiClient.getUserStats(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                txtEventosParticipados.setText(stats.eventsParticipated + " eventos realizados");
                txtProgressoPerfil.setText("Perfil " + stats.profileProgress + "% completo");
                
                if (stats.nextEventTitle != null && stats.nextEventDate != null) {
                    String dataFormatada = formatarData(stats.nextEventDate);
                    txtProximoEvento.setText("Próximo: " + stats.nextEventTitle + " - " + dataFormatada);
                } else {
                    txtProximoEvento.setText("Nenhum evento próximo");
                }
            });
        });
    }

    private void carregarPerfilVoluntario() {
        executor.execute(() -> {
            VolunteerApiClient.VolunteerProfile profile = volunteerApiClient.getVolunteerProfile(sessionManager.getAccessToken());
            runOnUiThread(() -> {
                // Marcar áreas
                checkBeleza.setChecked(profile.areas.contains("Beleza"));
                checkLogistica.setChecked(profile.areas.contains("Logística"));
                checkDivulgacao.setChecked(profile.areas.contains("Divulgação"));
                checkCaptacao.setChecked(profile.areas.contains("Captação"));
                
                // Marcar dias
                checkSegunda.setChecked(profile.availabilityDays.contains("Segunda"));
                checkTerca.setChecked(profile.availabilityDays.contains("Terça"));
                checkQuarta.setChecked(profile.availabilityDays.contains("Quarta"));
                checkQuinta.setChecked(profile.availabilityDays.contains("Quinta"));
                checkSexta.setChecked(profile.availabilityDays.contains("Sexta"));
                checkSabado.setChecked(profile.availabilityDays.contains("Sábado"));
                checkDomingo.setChecked(profile.availabilityDays.contains("Domingo"));
            });
        });
    }

    private void salvarPerfilVoluntario() {
        List<String> areas = new ArrayList<>();
        List<String> dias = new ArrayList<>();
        
        // Coletar áreas selecionadas
        if (checkBeleza.isChecked()) areas.add("Beleza");
        if (checkLogistica.isChecked()) areas.add("Logística");
        if (checkDivulgacao.isChecked()) areas.add("Divulgação");
        if (checkCaptacao.isChecked()) areas.add("Captação");
        
        // Coletar dias selecionados
        if (checkSegunda.isChecked()) dias.add("Segunda");
        if (checkTerca.isChecked()) dias.add("Terça");
        if (checkQuarta.isChecked()) dias.add("Quarta");
        if (checkQuinta.isChecked()) dias.add("Quinta");
        if (checkSexta.isChecked()) dias.add("Sexta");
        if (checkSabado.isChecked()) dias.add("Sábado");
        if (checkDomingo.isChecked()) dias.add("Domingo");
        
        executor.execute(() -> {
            boolean sucesso = volunteerApiClient.saveVolunteerProfile(sessionManager.getAccessToken(), areas, dias);
            runOnUiThread(() -> {
                if (sucesso) {
                    Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarEstatisticasUsuario(); // Atualiza o progresso
                } else {
                    Toast.makeText(this, "Erro ao salvar perfil", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String formatarData(String dataIso) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dataIso);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dataIso;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
