package com.example.extensao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    public interface OnVolunteerClickListener {
        void onVolunteerClick(Volunteer volunteer);
    }

    private List<Volunteer> volunteers;
    private final OnVolunteerClickListener listener;

    public VolunteerAdapter(List<Volunteer> volunteers, OnVolunteerClickListener listener) {
        this.volunteers = volunteers;
        this.listener = listener;
    }

    public void updateVolunteers(List<Volunteer> newVolunteers) {
        this.volunteers = newVolunteers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voluntario, parent, false);
        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder holder, int position) {
        Volunteer volunteer = volunteers.get(position);
        holder.bind(volunteer);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVolunteerClick(volunteer);
        });
    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }

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
            txtNome.setText(volunteer.name);
            txtEmail.setText(volunteer.email);

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
}
