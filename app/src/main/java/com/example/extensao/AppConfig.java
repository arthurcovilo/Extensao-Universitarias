package com.example.extensao;

/**
 * Configurações centralizadas do aplicativo.
 *
 * Para trocar o ambiente (dev → produção), altere apenas BASE_URL aqui.
 *
 * DEV  (emulador Android): "http://10.0.2.2:8080"
 * PROD (servidor real):    "https://sua-url-de-producao.com"
 */
public final class AppConfig {

    private AppConfig() {}

    /**
     * URL base da API backend.
     * Altere este valor para apontar para o servidor de produção antes de publicar.
     */
    public static final String BASE_URL = "http://10.0.2.2:8080";
}
