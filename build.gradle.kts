plugins {
    id("java")
}

group = "ru.webdevpet.server"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")  // Основная библиотека для работы с JSON
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")      // Основной компонент для работы с потоками JSON
}
tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.test {
    useJUnitPlatform()
}
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Запуск WebSocket и HTTP сервера"
    mainClass.set("ru.webdevpet.server.Main") // Укажите ваш основной класс
    classpath = sourceSets["main"].runtimeClasspath
}