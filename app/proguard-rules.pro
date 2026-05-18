# ============================================================
# ProGuard / R8 rules — Amor em Movimento
# ============================================================

# Preserva informações de linha para stack traces legíveis
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Modelos de dados (serialização JSON via org.json) ────────
# Mantém todos os campos públicos dos modelos para que o
# parsing manual via JSONObject.optString() continue funcionando.
-keep class com.example.extensao.Event { *; }
-keep class com.example.extensao.Volunteer { *; }
-keep class com.example.extensao.HistoricoItem { *; }
-keep class com.example.extensao.InscritoParticipacao { *; }
-keep class com.example.extensao.PostModel { *; }

# Inner classes dos API clients (LoginResult, ApiResult, etc.)
-keep class com.example.extensao.AuthApiClient$* { *; }
-keep class com.example.extensao.EventApiClient$* { *; }
-keep class com.example.extensao.VolunteerApiClient$* { *; }

# ── Google Identity / Credentials API ───────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# ── Glide ────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ── Material Design / AndroidX ───────────────────────────────
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ── org.json (incluído no Android SDK) ──────────────────────
-keep class org.json.** { *; }

# ── Suprime warnings de bibliotecas externas ────────────────
-dontwarn javax.annotation.**
-dontwarn kotlin.**
