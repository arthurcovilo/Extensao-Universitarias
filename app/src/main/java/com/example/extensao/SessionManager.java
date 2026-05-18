package com.example.extensao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Gerencia a sessão do usuário autenticado.
 *
 * Uso principal:
 *  - Salvar/recuperar token JWT e dados do usuário
 *  - Verificar se o usuário está logado
 *  - Detectar token expirado (HTTP 401/403) e redirecionar para Login
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "auth_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences preferences;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String userEmail, String userName, String userRole) {
        preferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_USER_EMAIL, userEmail)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_ROLE, userRole)
                .apply();
    }

    public boolean isLoggedIn() {
        String token = preferences.getString(KEY_ACCESS_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "USER");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getUserRole());
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }

    /**
     * Verifica se um código HTTP indica sessão expirada ou inválida.
     * Deve ser chamado após qualquer requisição autenticada que retorne 401 ou 403.
     *
     * Se o token estiver expirado, limpa a sessão e redireciona para LoginActivity.
     *
     * @param httpCode código de resposta HTTP recebido
     * @return true se a sessão foi invalidada e o redirecionamento foi disparado
     */
    public boolean handleUnauthorized(int httpCode) {
        if (httpCode == 401 || httpCode == 403) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Token inválido ou expirado (HTTP " + httpCode + "). Encerrando sessão.");
            }
            clearSession();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }
}
