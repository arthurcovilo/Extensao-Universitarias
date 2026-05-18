plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.extensao"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.extensao"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Habilita geração de BuildConfig (necessário para BuildConfig.DEBUG)
        // BASE_URL padrão (sobrescrita por cada buildType abaixo)
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
    }

    buildFeatures {
        buildConfig = true
    }

    // ─────────────────────────────────────────────────────────────
    // KEYSTORE DE PRODUÇÃO
    // ─────────────────────────────────────────────────────────────
    // Para gerar o APK release assinado, crie o arquivo keystore.properties
    // na raiz do projeto (NÃO commite esse arquivo no Git!) com o conteúdo:
    //
    //   storeFile=caminho/para/seu-keystore.jks
    //   storePassword=SUA_SENHA_STORE
    //   keyAlias=SEU_ALIAS
    //   keyPassword=SUA_SENHA_KEY
    //
    // Depois descomente o bloco signingConfigs e a linha signingConfig abaixo.
    // ─────────────────────────────────────────────────────────────
    //
    // val keystoreProps = java.util.Properties().also { props ->
    //     val propsFile = rootProject.file("keystore.properties")
    //     if (propsFile.exists()) props.load(propsFile.inputStream())
    // }
    //
    // signingConfigs {
    //     create("release") {
    //         storeFile     = file(keystoreProps["storeFile"] as String)
    //         storePassword = keystoreProps["storePassword"] as String
    //         keyAlias      = keystoreProps["keyAlias"] as String
    //         keyPassword   = keystoreProps["keyPassword"] as String
    //     }
    // }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ⚠️ Troque pela URL HTTPS real antes de gerar o APK de produção
            buildConfigField("String", "BASE_URL", "\"https://SUA-URL-DE-PRODUCAO.com\"")

            // Descomente após configurar o keystore acima:
            // signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            // URL do emulador Android para desenvolvimento local
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.cardview)
    
    // Glide for image loading
    implementation(libs.glide)
    
    // Google Credentials and Identity
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}