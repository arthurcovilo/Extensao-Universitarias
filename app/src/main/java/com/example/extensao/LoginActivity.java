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
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    // Web Client ID gerado no Google Cloud Console
    private static final String WEB_CLIENT_ID =
            "487621614650-ekd8795v6uu6ac2uco886h95f09lrta4.apps.googleusercontent.com";

    EditText email, senha;
    Button btnLogin, btnGoogleLogin;
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
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        progressLogin = findViewById(R.id.progressLogin);

        // Login com email/senha
        btnLogin.setOnClickListener(v -> {
            String user = email.getText().toString().trim();
            String pass = senha.getText().toString();

            if (!validarCampos(user, pass)) return;

            setLoading(true);
            executor.execute(() -> {
                AuthApiClient.LoginResult result = authApiClient.login(user, pass);
                runOnUiThread(() -> {
                    setLoading(false);
                    if (result.success) {
                        sessionManager.saveSession(result.accessToken, result.userEmail, result.userName, result.userRole);
                        abrirTelaPrincipal();
                    } else {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        });

        // Login com Google
        btnGoogleLogin.setOnClickListener(v -> iniciarLoginGoogle());
    }

    private void iniciarLoginGoogle() {
        setLoading(true);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(this);

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        runOnUiThread(() -> processarCredencialGoogle(response));
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            String msg = e.getMessage() != null ? e.getMessage() : "Erro ao entrar com Google";
                            // Usuário cancelou — não mostra erro
                            if (!msg.toLowerCase().contains("cancel")) {
                                Toast.makeText(LoginActivity.this, "Erro: " + msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
        );
    }

    private void processarCredencialGoogle(GetCredentialResponse response) {
        try {
            GoogleIdTokenCredential googleCredential =
                    GoogleIdTokenCredential.createFrom(response.getCredential().getData());

            String idToken = googleCredential.getIdToken();

            executor.execute(() -> {
                AuthApiClient.LoginResult result = authApiClient.loginComGoogle(idToken);
                runOnUiThread(() -> {
                    setLoading(false);
                    if (result.success) {
                        sessionManager.saveSession(result.accessToken, result.userEmail, result.userName, result.userRole);
                        abrirTelaPrincipal();
                    } else {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    }
                });
            });

        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Falha ao processar login Google", Toast.LENGTH_LONG).show();
        }
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
        btnGoogleLogin.setEnabled(!loading);
        email.setEnabled(!loading);
        senha.setEnabled(!loading);
    }

    private void abrirTelaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}