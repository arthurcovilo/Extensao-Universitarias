package com.example.extensao;

/**
 * Configurações centralizadas do aplicativo.
 *
 * ─────────────────────────────────────────────────────────────
 * CHECKLIST PARA PRODUÇÃO:
 * ─────────────────────────────────────────────────────────────
 * 1. Altere BASE_URL para a URL HTTPS do servidor de produção.
 *    Exemplo: "https://api.amoremmovimento.org.br"
 *
 * 2. Remova o bloco <domain-config> do arquivo:
 *    res/xml/network_security_config.xml
 *
 * 3. Certifique-se de que o backend está com HTTPS configurado
 *    (certificado SSL válido).
 *
 * 4. Altere o applicationId em build.gradle.kts para o package
 *    definitivo da ONG (ex: "br.org.amoremmovimento.app").
 * ─────────────────────────────────────────────────────────────
 */
public final class AppConfig {

    private AppConfig() {}

    /**
     * URL base da API backend.
     *
     * O valor é injetado automaticamente pelo Gradle em cada build type:
     *  - DEBUG   → http://10.0.2.2:8080  (emulador Android)
     *  - RELEASE → https://SUA-URL-DE-PRODUCAO.com
     *
     * Para trocar a URL de produção, edite build.gradle.kts:
     *   buildConfigField("String", "BASE_URL", "\"https://sua-url.com\"")
     *
     * ⚠️ NÃO altere este arquivo diretamente. Altere o build.gradle.kts.
     */
    public static final String BASE_URL = BuildConfig.BASE_URL;
}
