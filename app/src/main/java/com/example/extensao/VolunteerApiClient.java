package com.example.extensao;

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

    private static final String BASE_URL = "http://10.0.2.2:8080";

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

    public VolunteerProfile getVolunteerProfile(String accessToken) {
        try {
            URL url = new URL(BASE_URL + "/volunteer/profile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                VolunteerProfile profile = new VolunteerProfile();

                // Parse areas
                JSONArray areasArray = jsonResponse.optJSONArray("areas");
                if (areasArray != null) {
                    for (int i = 0; i < areasArray.length(); i++) {
                        profile.areas.add(areasArray.getString(i));
                    }
                }

                // Parse availability_days
                JSONArray daysArray = jsonResponse.optJSONArray("availability_days");
                if (daysArray != null) {
                    for (int i = 0; i < daysArray.length(); i++) {
                        profile.availabilityDays.add(daysArray.getString(i));
                    }
                }

                return profile;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new VolunteerProfile();
    }

    public boolean saveVolunteerProfile(String accessToken, List<String> areas, List<String> availabilityDays) {
        try {
            URL url = new URL(BASE_URL + "/volunteer/profile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            requestBody.put("areas", new JSONArray(areas));
            requestBody.put("availability_days", new JSONArray(availabilityDays));

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UserStats getUserStats(String accessToken) {
        try {
            URL url = new URL(BASE_URL + "/user/stats");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                UserStats stats = new UserStats();
                
                stats.eventsParticipated = jsonResponse.optInt("events_participated", 0);
                stats.profileProgress = jsonResponse.optInt("profile_progress", 0);

                JSONObject nextEvent = jsonResponse.optJSONObject("next_event");
                if (nextEvent != null) {
                    stats.nextEventTitle = nextEvent.optString("title");
                    stats.nextEventDate = nextEvent.optString("event_date");
                }

                return stats;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new UserStats();
    }

    public List<Volunteer> getVolunteers(String accessToken) {
        List<Volunteer> volunteers = new ArrayList<>();
        try {
            URL url = new URL(BASE_URL + "/volunteers");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return volunteers;
    }

    public static class VolunteerHistory {
        public String volunteerName;
        public String volunteerEmail;
        public List<Event> events;

        public VolunteerHistory() {
            this.events = new ArrayList<>();
        }
    }

    public VolunteerHistory getVolunteerHistory(String accessToken, String email) {
        VolunteerHistory history = new VolunteerHistory();
        try {
            URL url = new URL(BASE_URL + "/volunteers/" + java.net.URLEncoder.encode(email, "UTF-8") + "/history");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static class UserHistory {
        public int totalInscritos;
        public int totalParticipou;
        public int totalCancelado;
        public int totalNaoCompareceu;
        public List<HistoricoItem> historico;

        public UserHistory() {
            this.historico = new java.util.ArrayList<>();
        }
    }

    public UserHistory getUserHistory(String accessToken) {
        UserHistory result = new UserHistory();
        try {
            URL url = new URL(BASE_URL + "/user/history");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONObject resumo = json.optJSONObject("resumo");
                if (resumo != null) {
                    result.totalInscritos      = resumo.optInt("total_inscritos", 0);
                    result.totalParticipou     = resumo.optInt("total_participou", 0);
                    result.totalCancelado      = resumo.optInt("total_cancelado", 0);
                    result.totalNaoCompareceu  = resumo.optInt("total_nao_compareceu", 0);
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
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public AdminStats getAdminStats(String accessToken) {
        try {
            URL url = new URL(BASE_URL + "/admin/stats");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                AdminStats stats = new AdminStats();
                stats.totalVolunteers = jsonResponse.optInt("total_volunteers", 0);
                return stats;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new AdminStats();
    }
}