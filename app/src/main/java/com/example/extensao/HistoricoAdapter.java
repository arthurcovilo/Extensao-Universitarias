package com.example.extensao;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

    private List<HistoricoItem> items;

    public HistoricoAdapter(List<HistoricoItem> items) {
        this.items = items;
    }

    public void updateItems(List<HistoricoItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtData, txtLocal, txtStatus;

        public HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloHistorico);
            txtData   = itemView.findViewById(R.id.txtDataHistorico);
            txtLocal  = itemView.findViewById(R.id.txtLocalHistorico);
            txtStatus = itemView.findViewById(R.id.txtStatusParticipacao);
        }

        public void bind(HistoricoItem item) {
            txtTitulo.setText(item.title != null ? item.title : "Evento");
            txtLocal.setText(item.location != null && !item.location.isEmpty() ? item.location : "—");
            txtStatus.setText(item.getStatusLabel());
            txtStatus.setBackgroundColor(Color.parseColor(item.getStatusColor()));

            // Formata data
            try {
                String raw = item.eventDate;
                if (raw != null && raw.length() >= 10) raw = raw.substring(0, 10);
                SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = in.parse(raw);
                txtData.setText(out.format(date));
            } catch (ParseException | NullPointerException e) {
                txtData.setText(item.eventDate != null ? item.eventDate : "—");
            }
        }
    }
}
