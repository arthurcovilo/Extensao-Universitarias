package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ListView listViewNotificacoes;

    // Modelo simples de notificação
    static class Notificacao {
        String titulo;
        String subtitulo;
        String tempo;
        boolean lida;

        Notificacao(String titulo, String subtitulo, String tempo, boolean lida) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.tempo = tempo;
            this.lida = lida;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);

        listViewNotificacoes = findViewById(R.id.listViewNotificacoes);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notificacoes);

        // Lista de notificações de exemplo
        List<Notificacao> notificacoes = new ArrayList<>();
        notificacoes.add(new Notificacao("Evento amanhã: Workshop de TI", "Lembrete · 15/04 às 09h00", "agora", false));
        notificacoes.add(new Notificacao("Novo evento adicionado", "Palestra externa · 22/04 às 18h30", "2h", false));
        notificacoes.add(new Notificacao("Reunião geral em 1 semana", "Lembrete antecipado · 10/04", "5h", false));
        notificacoes.add(new Notificacao("Evento concluído: Intro Java", "07/04 · lida", "2d", true));
        notificacoes.add(new Notificacao("Bem-vindo ao app!", "01/04 · lida", "8d", true));

        ArrayAdapter<Notificacao> adapter = new ArrayAdapter<Notificacao>(this,
                R.layout.item_notificacao, notificacoes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_notificacao, parent, false);
                }
                Notificacao notif = getItem(position);
                TextView txtTitulo = convertView.findViewById(R.id.txtNotifTitulo);
                TextView txtSub = convertView.findViewById(R.id.txtNotifSub);
                TextView txtTempo = convertView.findViewById(R.id.txtNotifTempo);
                View dot = convertView.findViewById(R.id.viewDot);

                txtTitulo.setText(notif.titulo);
                txtSub.setText(notif.subtitulo);
                txtTempo.setText(notif.tempo);

                // Muda cor do dot se já foi lida
                if (notif.lida) {
                    dot.setBackgroundResource(R.drawable.dot_lida);
                    txtTitulo.setAlpha(0.5f);
                } else {
                    dot.setBackgroundResource(R.drawable.dot_nao_lida);
                    txtTitulo.setAlpha(1f);
                }
                return convertView;
            }
        };

        listViewNotificacoes.setAdapter(adapter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_calendario) {
                startActivity(new Intent(NotificacoesActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_eventos) {
                startActivity(new Intent(NotificacoesActivity.this, EventosActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(NotificacoesActivity.this, PerfilActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_contato) {
                startActivity(new Intent(NotificacoesActivity.this, ContatoActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_notificacoes) {
                return true;
            }

            return false;
        });
    }
}
