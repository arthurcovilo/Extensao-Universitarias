package com.example.extensao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOAD_MORE = 1;
    private static final int PAGE_SIZE = 15;

    public interface OnVolunteerClickListener {
        void onVolunteerClick(Volunteer volunteer);
    }

    private List<Volunteer> allVolunteers;
    private List<Volunteer> visibleVolunteers;
    private final OnVolunteerClickListener listener;
    private boolean hasMore = false;

    public VolunteerAdapter(List<Volunteer> volunteers, OnVolunteerClickListener listener) {
        this.allVolunteers = new ArrayList<>(volunteers);
        this.visibleVolunteers = new ArrayList<>();
        this.listener = listener;
        paginar();
    }

    private void paginar() {
        int currentSize = visibleVolunteers.size();
        int end = Math.min(currentSize + PAGE_SIZE, allVolunteers.size());
        for (int i = currentSize; i < end; i++) {
            visibleVolunteers.add(allVolunteers.get(i));
        }
        hasMore = visibleVolunteers.size() < allVolunteers.size();
    }

    public void loadMore() {
        int before = visibleVolunteers.size();
        paginar();
        notifyItemRangeInserted(before, visibleVolunteers.size() - before);
        notifyItemChanged(before - 1);
    }

    public void updateVolunteers(List<Volunteer> newVolunteers) {
        this.allVolunteers = new ArrayList<>(newVolunteers);
        this.visibleVolunteers = new ArrayList<>();
        paginar();
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voluntario, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bind(this);
            return;
        }
        Volunteer volunteer = visibleVolunteers.get(position);
        ((VolunteerViewHolder) holder).bind(volunteer);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVolunteerClick(volunteer);
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (hasMore && position == visibleVolunteers.size()) return VIEW_TYPE_LOAD_MORE;
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return visibleVolunteers.size() + (hasMore ? 1 : 0);
    }

    // ── ViewHolder de voluntário ─────────────────────────────────────────────
    static class VolunteerViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, txtAreas, txtDias, txtEventos, txtBadge;

        public VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome    = itemView.findViewById(R.id.txtNomeVoluntario);
            txtEmail   = itemView.findViewById(R.id.txtEmailVoluntario);
            txtAreas   = itemView.findViewById(R.id.txtAreasVoluntario);
            txtDias    = itemView.findViewById(R.id.txtDiasVoluntario);
            txtEventos = itemView.findViewById(R.id.txtEventosVoluntario);
            txtBadge   = itemView.findViewById(R.id.txtBadgeArea);
        }

        public void bind(Volunteer volunteer) {
            txtNome.setText(volunteer.name != null ? volunteer.name : "—");
            txtEmail.setText(volunteer.email != null ? volunteer.email : "—");

            if (volunteer.areas != null && !volunteer.areas.isEmpty()) {
                txtAreas.setText("Áreas: " + String.join(", ", volunteer.areas));
                txtBadge.setText(volunteer.areas.get(0));
                txtBadge.setVisibility(View.VISIBLE);
            } else {
                txtAreas.setText("Áreas: não informado");
                txtBadge.setVisibility(View.GONE);
            }

            if (volunteer.availabilityDays != null && !volunteer.availabilityDays.isEmpty()) {
                txtDias.setText("Dias: " + String.join(", ", volunteer.availabilityDays));
            } else {
                txtDias.setText("Dias: não informado");
            }

            String eventosText = volunteer.eventsParticipated == 1
                    ? "1 evento participado"
                    : volunteer.eventsParticipated + " eventos participados";
            txtEventos.setText(eventosText);
        }
    }

    // ── ViewHolder de "Carregar mais" ────────────────────────────────────────
    static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        Button btnLoadMore;

        public LoadMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btnLoadMore);
        }

        public void bind(VolunteerAdapter adapter) {
            btnLoadMore.setOnClickListener(v -> adapter.loadMore());
        }
    }
}
