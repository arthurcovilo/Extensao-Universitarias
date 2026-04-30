package com.example.extensao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;
    private boolean isAdmin;
    private List<Integer> registeredEventIds;

    public interface OnEventClickListener {
        void onRegisterClick(Event event, Button btnInscrever);
        void onEventClick(Event event);
        void onCardClick(Event event);
    }

    public EventAdapter(List<Event> events, boolean isAdmin, OnEventClickListener listener) {
        this.events = events;
        this.isAdmin = isAdmin;
        this.listener = listener;
        this.registeredEventIds = new ArrayList<>();
    }

    public void setRegisteredEventIds(List<Integer> ids) {
        this.registeredEventIds = ids != null ? ids : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloEvento, txtDataEvento, txtLocalEvento, txtDescricaoEvento, txtVagas, txtTipoEvento;
        Button btnInscrever;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloEvento = itemView.findViewById(R.id.txtTituloEvento);
            txtDataEvento = itemView.findViewById(R.id.txtDataEvento);
            txtLocalEvento = itemView.findViewById(R.id.txtLocalEvento);
            txtDescricaoEvento = itemView.findViewById(R.id.txtDescricaoEvento);
            txtVagas = itemView.findViewById(R.id.txtVagas);
            txtTipoEvento = itemView.findViewById(R.id.txtTipoEvento);
            btnInscrever = itemView.findViewById(R.id.btnInscrever);
        }

        public void bind(Event event) {
            txtTituloEvento.setText(event.title);
            txtDescricaoEvento.setText(event.description);
            txtVagas.setText(event.getVagasText());
            txtTipoEvento.setText(event.getTipoIcon() + " " + (event.eventType != null ? event.eventType : ""));

            // Local: oculta para Online, mostra ícone correto para os demais
            if ("Online".equals(event.eventType)) {
                txtLocalEvento.setText("💻 Online");
            } else if (event.location != null && !event.location.isEmpty()) {
                txtLocalEvento.setText("📍 " + event.location);
            } else {
                txtLocalEvento.setVisibility(View.GONE);
            }

            // Formatar data
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(event.eventDate);
                txtDataEvento.setText(outputFormat.format(date));
            } catch (ParseException e) {
                txtDataEvento.setText(event.eventDate);
            }

            // Click no card inteiro abre detalhes
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(event);
                }
            });

            // Configurar botão baseado no status e permissões
            if (isAdmin) {
                btnInscrever.setText("Gerenciar");
                btnInscrever.setEnabled(true);
                btnInscrever.setBackgroundTintList(null);
                btnInscrever.setOnClickListener(v -> {
                    if (listener != null) listener.onEventClick(event);
                });
            } else if (registeredEventIds.contains(event.id)) {
                // Já inscrito
                btnInscrever.setText("Inscrito ✓");
                btnInscrever.setEnabled(false);
                btnInscrever.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFAAAAAA));
            } else if (event.isOpen() && !event.isFull()) {
                btnInscrever.setText("Inscrever-se");
                btnInscrever.setEnabled(true);
                btnInscrever.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFF6750A4));
                btnInscrever.setOnClickListener(v -> {
                    if (listener != null) listener.onRegisterClick(event, btnInscrever);
                });
            } else if (event.isFull()) {
                btnInscrever.setText("Lotado");
                btnInscrever.setEnabled(false);
            } else {
                btnInscrever.setText("Encerrado");
                btnInscrever.setEnabled(false);
            }

            // Cor das vagas
            if (event.isFull()) {
                txtVagas.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            } else {
                txtVagas.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            }
        }
    }
}