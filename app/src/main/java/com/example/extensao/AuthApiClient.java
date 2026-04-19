package com.example.extensao;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AuthApiClient {

    // Para emulador Android apontando para backend local na máquina host.
    // Troque para sua URL de produção quando publicar.
    private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final String LOGIN_ENDPOINT = "/auth/login";

    public LoginResult login(String email, String senha) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + LOGIN_ENDPOINT);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", senha);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readBody(responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream());

            if (responseCode >= 200 && responseCode < 300) {
                JSONObject json = new JSONObject(responseBody);
                String token = json.optString("accessToken", "");
                JSONObject user = json.optJSONObject("user");
                String userName = user != null ? user.optString("name", "") : "";
                String userEmail = user != null ? user.optString("email", email) : email;

                if (token.trim().isEmpty()) {
                    return LoginResult.error("Resposta inválida do servidor (token ausente)");
                }

                return LoginResult.success(token, userEmail, userName);
            }

            JSONObject errorJson = tryParseJson(responseBody);
            String errorMessage = errorJson != null
                    ? errorJson.optString("message", "Credenciais inválidas")
                    : "Credenciais inválidas";

            if (responseCode >= 500) {
                errorMessage = "Servidor indisponível. Tente novamente em instantes.";
            }

            return LoginResult.error(errorMessage);

        } catch (Exception e) {
            return LoginResult.error("Falha de conexão. Verifique internet e URL da API.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final String accessToken;
        public final String userEmail;
        public final String userName;

        private LoginResult(boolean success, String message, String accessToken, String userEmail, String userName) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
            this.userEmail = userEmail;
            this.userName = userName;
        }

        public static LoginResult success(String accessToken, String userEmail, String userName) {
            return new LoginResult(true, "Login realizado com sucesso", accessToken, userEmail, userName);
        }

        public static LoginResult error(String message) {
            return new LoginResult(false, message, "", "", "");
        }
    }
}
