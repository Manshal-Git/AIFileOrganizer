import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.core)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // kotlin-logging (ours) and Koog both log through SLF4J; without a provider on the runtime
    // classpath every line is silently dropped ("No SLF4J providers were found").
    runtimeOnly(libs.slf4j.simple)
}

compose.desktop {
    application {
        mainClass = "com.manshal79.aifileorganizer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.manshal79.aifileorganizer"
            packageVersion = "1.0.0"
        }
    }
}