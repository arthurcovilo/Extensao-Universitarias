package com.example.extensao;

public class InscritoParticipacao {
    public int userId;
    public String name;
    public String email;
    public String participationStatus; // INSCRITO, PARTICIPOU, CANCELADO, NAO_COMPARECEU

    public String getStatusLabel() {
        if (participationStatus == null) return "Inscrito";
        switch (participationStatus) {
            case "PARTICIPOU":     return "Participou";
            case "CANCELADO":      return "Cancelado";
            case "NAO_COMPARECEU": return "Não compareceu";
            default:               return "Inscrito";
        }
    }

    public int getStatusColor() {
        if (participationStatus == null) return 0xFF6750A4;
        switch (participationStatus) {
            case "PARTICIPOU":     return 0xFF2E7D32;
            case "CANCELADO":      return 0xFFB71C1C;
            case "NAO_COMPARECEU": return 0xFFE65100;
            default:               return 0xFF6750A4;
        }
    }
}
