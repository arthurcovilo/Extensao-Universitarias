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