package com.example.extensao;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Cliente da API do feed de avisos (/posts)
 */
public class PostApiClient {

    private static final String TAG = "PostApiClient";
    private static final String BASE_URL = AppConfig.BASE_URL;
    private static final String APP_TIMEZONE = "America/Sao_Paulo";

    private final SessionManager sessionManager;

    public PostApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Busca os posts mais recentes.
     * Lança exceção em falha de rede ou resposta de erro, para a tela
     * diferenciar "erro" de "lista vazia".
     */
    public List<PostModel> getPosts(String accessToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/posts");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (sessionManager.handleUnauthorized(responseCode)) {
                throw new IOException(SessionManager.SESSION_EXPIRED_MESSAGE);
            }
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("HTTP " + responseCode);
            }

            JSONArray jsonArray = new JSONArray(readBody(connection.getInputStream()));
            List<PostModel> posts = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                posts.add(fromJson(jsonArray.getJSONObject(i)));
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "getPosts: " + posts.size() + " posts carregados");
            return posts;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public EventApiClient.ApiResult createPost(String title, String content, String accessToken) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/posts");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("title", title);
            body.put("content", content);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (sessionManager.handleUnauthorized(responseCode)) {
                return EventApiClient.ApiResult.error(SessionManager.SESSION_EXPIRED_MESSAGE);
            }
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return EventApiClient.ApiResult.success("Aviso publicado com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao publicar aviso")
                        : "Erro ao publicar aviso";
                return EventApiClient.ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return EventApiClient.ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public EventApiClient.ApiResult deletePost(String postId, String accessToken) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + "/posts/" + postId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (sessionManager.handleUnauthorized(responseCode)) {
                return EventApiClient.ApiResult.error(SessionManager.SESSION_EXPIRED_MESSAGE);
            }
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                return EventApiClient.ApiResult.success("Aviso excluído com sucesso!");
            } else {
                JSONObject errorJson = tryParseJson(responseBody);
                String errorMessage = errorJson != null
                        ? errorJson.optString("message", "Erro ao excluir aviso")
                        : "Erro ao excluir aviso";
                return EventApiClient.ApiResult.error(errorMessage);
            }

        } catch (Exception e) {
            return EventApiClient.ApiResult.error("Falha de conexão. Verifique sua internet.");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * Converte um post da API para o modelo do feed.
     * v1 só texto: sem imagem e sem local (o card esconde os dois).
     */
    private static PostModel fromJson(JSONObject json) {
        return new PostModel(
                String.valueOf(json.optInt("id")),
                json.optString("title", ""),
                json.optString("content", ""),
                null,
                json.isNull("created_at") ? "" : formatarData(json.optString("created_at", "")),
                null
        );
    }

    /**
     * created_at vem em ISO UTC (ex.: "2026-11-01T21:30:00.000Z");
     * exibe no horário de São Paulo.
     */
    private static String formatarData(String isoUtc) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getTimeZone(APP_TIMEZONE));
            Date date = inputFormat.parse(isoUtc);
            return date != null ? outputFormat.format(date) : isoUtc;
        } catch (Exception e) {
            return isoUtc;
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
}
