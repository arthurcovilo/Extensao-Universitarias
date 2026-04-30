package com.example.extensao;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CustomCalendarAdapter extends RecyclerView.Adapter<CustomCalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(int year, int month, int day);
    }

    // Cores por tipo de evento
    private static final Map<String, Integer> TIPO_CORES = new java.util.HashMap<String, Integer>() {{
        put("Presencial",       Color.parseColor("#1976D2")); // azul
        put("Online",           Color.parseColor("#6750A4")); // roxo
        put("Retirada de Itens",Color.parseColor("#E65100")); // laranja
        put("Doação",           Color.parseColor("#2E7D32")); // verde
    }};

    private final Context context;
    private final List<Integer> days;       // 0 = célula vazia (padding), >0 = dia real
    private final int year;
    private final int month;                // 0-based
    private int selectedDay = -1;
    private Map<String, List<Event>> eventsByDate = new java.util.HashMap<>();
    private OnDayClickListener listener;

    public CustomCalendarAdapter(Context context, int year, int month) {
        this.context = context;
        this.year = year;
        this.month = month;
        this.days = buildDays(year, month);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setEventsByDate(Map<String, List<Event>> eventsByDate) {
        this.eventsByDate = eventsByDate;
        notifyDataSetChanged();
    }

    public void setSelectedDay(int day) {
        this.selectedDay = day;
        notifyDataSetChanged();
    }

    private List<Integer> buildDays(int year, int month) {
        List<Integer> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        // Dia da semana do primeiro dia (0=Dom, 1=Seg, ...)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday=0
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Células vazias antes do dia 1
        for (int i = 0; i < firstDayOfWeek; i++) list.add(0);
        // Dias do mês
        for (int d = 1; d <= daysInMonth; d++) list.add(d);
        // Completar última linha
        while (list.size() % 7 != 0) list.add(0);

        return list;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_dia_calendario, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = days.get(position);

        if (day == 0) {
            // Célula vazia
            holder.txtDia.setText("");
            holder.txtDia.setBackground(null);
            holder.layoutPontinhos.removeAllViews();
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.txtDia.setText(String.valueOf(day));

        // Destaque do dia selecionado
        if (day == selectedDay) {
            holder.txtDia.setBackgroundResource(R.drawable.bg_dia_selecionado);
            holder.txtDia.setTextColor(Color.WHITE);
            holder.txtDia.setTypeface(null, Typeface.BOLD);
        } else {
            // Destaque do dia atual
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) == year
                    && today.get(Calendar.MONTH) == month
                    && today.get(Calendar.DAY_OF_MONTH) == day) {
                holder.txtDia.setBackgroundResource(R.drawable.bg_dia_hoje);
                holder.txtDia.setTextColor(Color.parseColor("#6750A4"));
                holder.txtDia.setTypeface(null, Typeface.BOLD);
            } else {
                holder.txtDia.setBackground(null);
                holder.txtDia.setTextColor(Color.parseColor("#222222"));
                holder.txtDia.setTypeface(null, Typeface.NORMAL);
            }
        }

        // Pontinhos de eventos
        holder.layoutPontinhos.removeAllViews();
        String dateKey = String.format(java.util.Locale.getDefault(),
                "%04d-%02d-%02d", year, month + 1, day);
        List<Event> eventos = eventsByDate.get(dateKey);

        if (eventos != null && !eventos.isEmpty()) {
            // Coleta tipos únicos do dia
            java.util.Set<String> tiposVistos = new java.util.LinkedHashSet<>();
            for (Event e : eventos) {
                if (e.eventType != null) tiposVistos.add(e.eventType);
            }
            for (String tipo : tiposVistos) {
                View dot = new View(context);
                int size = dpToPx(6);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(dpToPx(1), 0, dpToPx(1), 0);
                dot.setLayoutParams(params);
                Integer cor = TIPO_CORES.get(tipo);
                dot.setBackgroundColor(cor != null ? cor : Color.GRAY);
                // Pontinho circular
                android.graphics.drawable.GradientDrawable circle =
                        new android.graphics.drawable.GradientDrawable();
                circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                circle.setColor(cor != null ? cor : Color.GRAY);
                dot.setBackground(circle);
                holder.layoutPontinhos.addView(dot);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(year, month, day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView txtDia;
        LinearLayout layoutPontinhos;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDia = itemView.findViewById(R.id.txtDia);
            layoutPontinhos = itemView.findViewById(R.id.layoutPontinhos);
        }
    }
}
