// Top-level build file — configuração comum a todos os módulos.
plugins {
    alias(libs.plugins.android.application) apply false
}

// -----------------------------------------------------------------------------
// Redirecionamento OPCIONAL da saída de build.
// Em um clone normal o projeto usa o diretório padrão "build/".
// Caso o projeto esteja dentro de uma pasta sincronizada (ex.: OneDrive), que
// pode travar arquivos e quebrar as operações incrementais do Gradle, defina a
// propriedade "calmerExternalBuildDir" (em ~/.gradle/gradle.properties ou via
// -PcalmerExternalBuildDir=/caminho) para mover a saída para fora da sincronização.
// -----------------------------------------------------------------------------
val externalBuildDir = providers.gradleProperty("calmerExternalBuildDir").orNull
if (externalBuildDir != null) {
    val root = File(externalBuildDir)
    layout.buildDirectory.set(root.resolve("root"))
    subprojects {
        layout.buildDirectory.set(root.resolve(name))
    }
}
