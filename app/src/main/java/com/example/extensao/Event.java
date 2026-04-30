package com.example.extensao;

import org.json.JSONObject;

public class Event {

    // Tipos de evento disponíveis
    public static final String[] TIPOS = {
        "Presencial", "Online", "Retirada de Itens", "Doação"
    };

    public int id;
    public String title;
    public String description;
    public String eventDate;
    public String location;
    public String status;
    public String eventType;
    public int maxParticipants;
    public int registeredCount;
    public String createdByName;

    public Event() {}

    public Event(String title, String description, String eventDate, String location, int maxParticipants) {
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.status = "ABERTO";
    }

    public static Event fromJson(JSONObject json) {
        Event event = new Event();
        event.id = json.optInt("id");
        event.title = json.optString("title", "");
        event.description = json.optString("description", "");
        event.eventDate = json.optString("event_date", "");
        event.location = json.optString("location", "");
        event.status = json.optString("status", "ABERTO");
        event.eventType = json.optString("event_type", "Presencial");
        event.maxParticipants = json.optInt("max_participants", 0);
        event.registeredCount = json.optInt("registered_count", 0);
        event.createdByName = json.optString("created_by_name", "");
        return event;
    }

    public boolean isOpen() {
        return "ABERTO".equals(status);
    }

    public boolean isFull() {
        return maxParticipants > 0 && registeredCount >= maxParticipants;
    }

    public String getVagasText() {
        if (maxParticipants > 0) {
            int vagas = maxParticipants - registeredCount;
            return vagas > 0 ? String.valueOf(vagas) : "Lotado";
        }
        return "∞";
    }

    // Retorna ícone emoji para cada tipo
    public String getTipoIcon() {
        if (eventType == null) return "📌";
        switch (eventType) {
            case "Presencial":      return "📍";
            case "Online":         return "💻";
            case "Retirada de Itens": return "📦";
            case "Doação":         return "🤝";
            default:               return "📌";
        }
    }
}