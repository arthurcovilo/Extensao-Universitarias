package com.example.extensao;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    EditText email, senha;
    Button btnLogin;
    ProgressBar progressLogin;

    private SessionManager sessionManager;
    private AuthApiClient authApiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        authApiClient = new AuthApiClient();

        if (sessionManager.isLoggedIn()) {
            abrirTelaPrincipal();
            return;
        }

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.editEmail);
        senha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);

        btnLogin.setOnClickListener(v -> {
            String user = email.getText().toString().trim();
            String pass = senha.getText().toString();

            if (!validarCampos(user, pass)) {
                return;
            }

            setLoading(true);

            executor.execute(() -> {
                AuthApiClient.LoginResult result = authApiClient.login(user, pass);

                runOnUiThread(() -> {
                    setLoading(false);

                    if (result.success) {
                        sessionManager.saveSession(result.accessToken, result.userEmail, result.userName);
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
                        abrirTelaPrincipal();
                    } else {
                        Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }

    private boolean validarCampos(String user, String pass) {
        if (TextUtils.isEmpty(user)) {
            email.setError("Informe seu email");
            email.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            email.setError("Informe um email válido");
            email.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            senha.setError("Informe sua senha");
            senha.requestFocus();
            return false;
        }

        if (pass.length() < 6) {
            senha.setError("A senha deve ter ao menos 6 caracteres");
            senha.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        email.setEnabled(!loading);
        senha.setEnabled(!loading);
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
