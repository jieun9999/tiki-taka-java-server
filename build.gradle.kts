
plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation ("com.google.firebase:firebase-admin:9.2.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    // Firebase 서버로 푸시 메시지 전송 시 필요
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("chatServer")
    archiveVersion.set("1.0-SNAPSHOT")
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "chat.Server"
    }
}

