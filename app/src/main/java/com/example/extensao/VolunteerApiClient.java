package com.example.extensao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VolunteerApiClient {

    private static final String TAG = "VolunteerApiClient";
    private static final String BASE_URL = AppConfig.BASE_URL;

    // ── Inner classes ────────────────────────────────────────────────────────

    public static class VolunteerProfile {
        public List<String> areas;
        public List<String> availabilityDays;

        public VolunteerProfile() {
            this.areas = new ArrayList<>();
            this.availabilityDays = new ArrayList<>();
        }
    }

    public static class UserStats {
        public int eventsParticipated;
        public String nextEventTitle;
        public String nextEventDate;
        public int profileProgress;

        public UserStats() {
            this.eventsParticipated = 0;
            this.nextEventTitle = null;
            this.nextEventDate = null;
            this.profileProgress = 0;
        }
    }

    public static class AdminStats {
        public int totalVolunteers;

        public AdminStats() {
            this.totalVolunteers = 0;
        }
    }

    public static class VolunteerHistory {
        public String volunteerName;
        public String volunteerEmail;
        public List<Event> events;

        public VolunteerHistory() {
            this.events = new ArrayList<>();
        }
    }

    public static class UserHistory {
        public int totalInscritos;
        public int totalParticipou;
        public int totalCancelado;
        public int totalNaoCompareceu;
        public List<HistoricoItem> historico;

        public UserHistory() {
            this.historico = new ArrayList<>();
        }
    }

    // ── Perfil de voluntário ─────────────────────────────────────────────────

    public VolunteerProfile getVolunteerProfile(String accessToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/volunteer/profile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONObject json = new JSONObject(body);
                VolunteerProfile profile = new VolunteerProfile();

                JSONArray areasArray = json.optJSONArray("areas");
                if (areasArray != null) {
                    for (int i = 0; i < areasArray.length(); i++) {
                        profile.areas.add(areasArray.getString(i));
                    }
                }

                JSONArray daysArray = json.optJSONArray("availability_days");
                if (daysArray != null) {
                    for (int i = 0; i < daysArray.length(); i++) {
                        profile.availabilityDays.add(daysArray.getString(i));
                    }
                }
                return profile;
            }
            // Trata sessão expirada silenciosamente — retorna perfil vazio
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "getVolunteerProfile: HTTP " + responseCode);
            }
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar perfil de voluntário", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return new VolunteerProfile();
    }

    public boolean saveVolunteerProfile(String accessToken, List<String> areas, List<String> availabilityDays) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/volunteer/profile");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("areas", new JSONArray(areas));
            requestBody.put("availability_days", new JSONArray(availabilityDays));

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
            }

            int responseCode = connection.getResponseCode();
            if (BuildConfig.DEBUG) Log.d(TAG, "saveVolunteerProfile: HTTP " + responseCode);
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao salvar perfil de voluntário", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return false;
    }

    // ── Estatísticas ─────────────────────────────────────────────────────────

    public UserStats getUserStats(String accessToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/user/stats");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONObject json = new JSONObject(body);
                UserStats stats = new UserStats();
                stats.eventsParticipated = json.optInt("events_participated", 0);
                stats.profileProgress = json.optInt("profile_progress", 0);

                JSONObject nextEvent = json.optJSONObject("next_event");
                if (nextEvent != null) {
                    stats.nextEventTitle = nextEvent.optString("title");
                    stats.nextEventDate = nextEvent.optString("event_date");
                }
                return stats;
            }
            if (BuildConfig.DEBUG) Log.w(TAG, "getUserStats: HTTP " + responseCode);
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar estatísticas do usuário", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return new UserStats();
    }

    public AdminStats getAdminStats(String accessToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/admin/stats");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONObject json = new JSONObject(body);
                AdminStats stats = new AdminStats();
                stats.totalVolunteers = json.optInt("total_volunteers", 0);
                return stats;
            }
            if (BuildConfig.DEBUG) Log.w(TAG, "getAdminStats: HTTP " + responseCode);
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar estatísticas do admin", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return new AdminStats();
    }

    // ── Voluntários ──────────────────────────────────────────────────────────

    public List<Volunteer> getVolunteers(String accessToken) {
        List<Volunteer> volunteers = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/volunteers");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONArray jsonArray = new JSONArray(body);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Volunteer v = new Volunteer();
                    v.name = obj.optString("name");
                    v.email = obj.optString("email");
                    v.eventsParticipated = obj.optInt("events_participated", 0);
                    v.totalHours = obj.optInt("total_hours", 0);

                    JSONArray areasArray = obj.optJSONArray("areas");
                    v.areas = new ArrayList<>();
                    if (areasArray != null) {
                        for (int j = 0; j < areasArray.length(); j++) {
                            v.areas.add(areasArray.getString(j));
                        }
                    }

                    JSONArray daysArray = obj.optJSONArray("availability_days");
                    v.availabilityDays = new ArrayList<>();
                    if (daysArray != null) {
                        for (int j = 0; j < daysArray.length(); j++) {
                            v.availabilityDays.add(daysArray.getString(j));
                        }
                    }
                    volunteers.add(v);
                }
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "getVolunteers: " + volunteers.size() + " encontrados");
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar voluntários", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return volunteers;
    }

    public VolunteerHistory getVolunteerHistory(String accessToken, String email) {
        VolunteerHistory history = new VolunteerHistory();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/volunteers/" + java.net.URLEncoder.encode(email, "UTF-8") + "/history");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONObject json = new JSONObject(body);
                JSONObject volunteerJson = json.optJSONObject("volunteer");
                if (volunteerJson != null) {
                    history.volunteerName = volunteerJson.optString("name");
                    history.volunteerEmail = volunteerJson.optString("email");
                }

                JSONArray eventsArray = json.optJSONArray("events");
                if (eventsArray != null) {
                    for (int i = 0; i < eventsArray.length(); i++) {
                        history.events.add(Event.fromJson(eventsArray.getJSONObject(i)));
                    }
                }
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "getVolunteerHistory: HTTP " + responseCode);
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar histórico do voluntário", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return history;
    }

    // ── Histórico do usuário logado ──────────────────────────────────────────

    public UserHistory getUserHistory(String accessToken) {
        UserHistory result = new UserHistory();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/user/history");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String body = readBody(connection.getInputStream());
                JSONObject json = new JSONObject(body);
                JSONObject resumo = json.optJSONObject("resumo");
                if (resumo != null) {
                    result.totalInscritos     = resumo.optInt("total_inscritos", 0);
                    result.totalParticipou    = resumo.optInt("total_participou", 0);
                    result.totalCancelado     = resumo.optInt("total_cancelado", 0);
                    result.totalNaoCompareceu = resumo.optInt("total_nao_compareceu", 0);
                }

                JSONArray historico = json.optJSONArray("historico");
                if (historico != null) {
                    for (int i = 0; i < historico.length(); i++) {
                        JSONObject obj = historico.getJSONObject(i);
                        HistoricoItem item = new HistoricoItem();
                        item.eventId             = obj.optInt("event_id");
                        item.title               = obj.optString("title");
                        item.eventDate           = obj.optString("event_date");
                        item.location            = obj.optString("location");
                        item.eventStatus         = obj.optString("event_status");
                        item.registeredAt        = obj.optString("registered_at");
                        item.participationStatus = obj.optString("participation_status", "INSCRITO");
                        result.historico.add(item);
                    }
                }
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "getUserHistory: HTTP " + responseCode);
        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Erro ao buscar histórico do usuário", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return result;
    }

    // ── Utilitário ───────────────────────────────────────────────────────────

    private String readBody(java.io.InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
