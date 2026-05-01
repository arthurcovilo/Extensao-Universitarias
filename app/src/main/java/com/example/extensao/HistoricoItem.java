package com.example.extensao;

public class HistoricoItem {
    public int eventId;
    public String title;
    public String eventDate;
    public String location;
    public String eventStatus;
    public String registeredAt;
    public String participationStatus;

    public String getStatusLabel() {
        if (participationStatus == null) return "Inscrito";
        switch (participationStatus) {
            case "PARTICIPOU":       return "Participou";
            case "CANCELADO":        return "Cancelado";
            case "NAO_COMPARECEU":   return "Não compareceu";
            default:                 return "Inscrito";
        }
    }

    // Cor associada ao status: retorna string de cor hex
    public String getStatusColor() {
        if (participationStatus == null) return "#6750A4";
        switch (participationStatus) {
            case "PARTICIPOU":       return "#2E7D32"; // verde
            case "CANCELADO":        return "#B71C1C"; // vermelho
            case "NAO_COMPARECEU":   return "#E65100"; // laranja
            default:                 return "#6750A4"; // roxo (inscrito)
        }
    }
}
