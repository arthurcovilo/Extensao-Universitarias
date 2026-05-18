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

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOAD_MORE = 1;
    private static final int PAGE_SIZE = 10;

    private List<Event> allEvents;
    private List<Event> visibleEvents;
    private OnEventClickListener listener;
    private boolean isAdmin;
    private List<Integer> registeredEventIds;
    private boolean hasMore = false;

    public interface OnEventClickListener {
        void onRegisterClick(Event event, Button btnInscrever);
        void onCancelClick(Event event, Button btnInscrever);
        void onEventClick(Event event);
        void onCardClick(Event event);
    }

    public EventAdapter(List<Event> events, boolean isAdmin, OnEventClickListener listener) {
        this.allEvents = new ArrayList<>(events);
        this.visibleEvents = new ArrayList<>();
        this.isAdmin = isAdmin;
        this.listener = listener;
        this.registeredEventIds = new ArrayList<>();
        paginar();
    }

    private void paginar() {
        int currentSize = visibleEvents.size();
        int end = Math.min(currentSize + PAGE_SIZE, allEvents.size());
        for (int i = currentSize; i < end; i++) {
            visibleEvents.add(allEvents.get(i));
        }
        hasMore = visibleEvents.size() < allEvents.size();
    }

    public void loadMore() {
        int before = visibleEvents.size();
        paginar();
        notifyItemRangeInserted(before, visibleEvents.size() - before);
        // Atualiza o item "Carregar mais" (último antes da inserção)
        notifyItemChanged(before - 1);
    }

    public void setRegisteredEventIds(List<Integer> ids) {
        this.registeredEventIds = ids != null ? ids : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bind(this);
            return;
        }
        Event event = visibleEvents.get(position);
        ((EventViewHolder) holder).bind(event);
    }

    @Override
    public int getItemViewType(int position) {
        if (hasMore && position == visibleEvents.size()) return VIEW_TYPE_LOAD_MORE;
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return visibleEvents.size() + (hasMore ? 1 : 0);
    }

    public void updateEvents(List<Event> newEvents) {
        this.allEvents = new ArrayList<>(newEvents);
        this.visibleEvents = new ArrayList<>();
        paginar();
        notifyDataSetChanged();
    }

    // ── ViewHolder de item de evento ─────────────────────────────────────────
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
            txtTipoEvento.setText(event.eventType != null ? event.eventType : "");

            // Local: oculta para Online, mostra ícone correto para os demais
            txtLocalEvento.setVisibility(View.VISIBLE);
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
                if (listener != null) listener.onCardClick(event);
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
                btnInscrever.setText("Cancelar inscrição");
                btnInscrever.setEnabled(true);
                btnInscrever.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFAAAAAA));
                btnInscrever.setOnClickListener(v -> {
                    if (listener != null) listener.onCancelClick(event, btnInscrever);
                });
            } else if (event.isOpen() && !event.isFull()) {
                btnInscrever.setText("Inscrever-se");
                btnInscrever.setEnabled(true);
                btnInscrever.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFD32F2F));
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

    // ── ViewHolder de "Carregar mais" ────────────────────────────────────────
    static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        Button btnLoadMore;

        public LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btnLoadMore);
        }

        public void bind(EventAdapter adapter) {
            btnLoadMore.setOnClickListener(v -> adapter.loadMore());
        }
    }
}