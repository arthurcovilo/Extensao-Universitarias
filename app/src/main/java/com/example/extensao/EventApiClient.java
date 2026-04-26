package com.example.extensao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EventApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080";

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/events");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String responseBody = readBody(connection.getInputStream());
                JSONArray jsonArray = new JSONArray(responseBody);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject eventJson = jsonArray.getJSONObject(i);
                    Event event = Event.fromJson(eventJson);
                    events.add(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }

        return events;
    }

    public ApiResult registerForEvent(int eventId, String accessToken) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/events/" + eventId + "/register");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return ApiResult.success("Inscrição realizada com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao se inscrever")
                        : "Erro ao se inscrever";
                return ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public ApiResult createEvent(Event event, String accessToken) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/events");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("title", event.title);
            body.put("description", event.description);
            body.put("event_date", event.eventDate);
            body.put("location", event.location);
            if (event.maxParticipants > 0) {
                body.put("max_participants", event.maxParticipants);
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return ApiResult.success("Evento criado com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao criar evento")
                        : "Erro ao criar evento";
                return ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public ApiResult updateEvent(Event event, String accessToken) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/events/" + event.id);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("title", event.title);
            body.put("description", event.description);
            body.put("event_date", event.eventDate);
            body.put("location", event.location);
            body.put("status", event.status);
            if (event.maxParticipants > 0) {
                body.put("max_participants", event.maxParticipants);
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return ApiResult.success("Evento atualizado com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao atualizar evento")
                        : "Erro ao atualizar evento";
                return ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public boolean isUserRegistered(int eventId, String accessToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + "/events/" + eventId + "/is-registered");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String body = readBody(connection.getInputStream());
                JSONObject json = tryParseJson(body);
                return json != null && json.optBoolean("registered", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }
        return false;
    }

    public ApiResult deleteEvent(int eventId, String accessToken) {        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/events/" + eventId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return ApiResult.success("Evento excluído com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao excluir evento")
                        : "Erro ao excluir evento";
                return ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String readBody(InputStream stream) throws Exception {
        if (stream == null) return "";

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private JSONObject tryParseJson(String raw) {
        try {
            return new JSONObject(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class ApiResult {
        public final boolean success;
        public final String message;

        private ApiResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ApiResult success(String message) {
            return new ApiResult(true, message);
        }

        public static ApiResult error(String message) {
            return new ApiResult(false, message);
        }
    }
}