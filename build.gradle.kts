plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

if (JavaVersion.current() < JavaVersion.VERSION_17 || JavaVersion.VERSION_21 < JavaVersion.current()) {
    throw GradleException("""
            You are currently targeting a JDK for Java version ${JavaVersion.current()};
            you must use a JDK 17-21 to build this project.
            """)
}
