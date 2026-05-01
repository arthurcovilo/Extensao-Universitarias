package com.example.extensao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InscritosParticipacaoAdapter
        extends RecyclerView.Adapter<InscritosParticipacaoAdapter.ViewHolder> {

    public interface OnStatusChangeListener {
        void onStatusChange(InscritoParticipacao inscrito, String novoStatus);
    }

    private List<InscritoParticipacao> inscritos;
    private final OnStatusChangeListener listener;

    public InscritosParticipacaoAdapter(List<InscritoParticipacao> inscritos,
                                        OnStatusChangeListener listener) {
        this.inscritos = inscritos;
        this.listener  = listener;
    }

    public void updateInscritos(List<InscritoParticipacao> novos) {
        this.inscritos = novos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inscrito_participacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InscritoParticipacao inscrito = inscritos.get(position);
        holder.bind(inscrito, listener);
    }

    @Override
    public int getItemCount() {
        return inscritos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, txtStatusAtual;
        Button btnParticipou, btnNaoCompareceu, btnCancelado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome          = itemView.findViewById(R.id.txtNomeInscrito);
            txtEmail         = itemView.findViewById(R.id.txtEmailInscrito);
            txtStatusAtual   = itemView.findViewById(R.id.txtStatusAtual);
            btnParticipou    = itemView.findViewById(R.id.btnParticipou);
            btnNaoCompareceu = itemView.findViewById(R.id.btnNaoCompareceu);
            btnCancelado     = itemView.findViewById(R.id.btnCancelado);
        }

        public void bind(InscritoParticipacao inscrito, OnStatusChangeListener listener) {
            txtNome.setText(inscrito.name);
            txtEmail.setText(inscrito.email);
            txtStatusAtual.setText(inscrito.getStatusLabel());
            txtStatusAtual.setBackgroundColor(inscrito.getStatusColor());

            btnParticipou.setOnClickListener(v -> {
                inscrito.participationStatus = "PARTICIPOU";
                txtStatusAtual.setText(inscrito.getStatusLabel());
                txtStatusAtual.setBackgroundColor(inscrito.getStatusColor());
                if (listener != null) listener.onStatusChange(inscrito, "PARTICIPOU");
            });

            btnNaoCompareceu.setOnClickListener(v -> {
                inscrito.participationStatus = "NAO_COMPARECEU";
                txtStatusAtual.setText(inscrito.getStatusLabel());
                txtStatusAtual.setBackgroundColor(inscrito.getStatusColor());
                if (listener != null) listener.onStatusChange(inscrito, "NAO_COMPARECEU");
            });

            btnCancelado.setOnClickListener(v -> {
                inscrito.participationStatus = "CANCELADO";
                txtStatusAtual.setText(inscrito.getStatusLabel());
                txtStatusAtual.setBackgroundColor(inscrito.getStatusColor());
                if (listener != null) listener.onStatusChange(inscrito, "CANCELADO");
            });
        }
    }
}
