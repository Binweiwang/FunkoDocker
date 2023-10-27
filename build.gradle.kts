plugins {
    id("java")
    // Para que funcione el shadowJar
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // R2DBC
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    // Logger
    implementation("ch.qos.logback:logback-classic:1.4.11")
    // Project Reactor
    implementation("io.projectreactor:reactor-core:3.5.10")
    // Lombook para generar código, poner todo esto para que funcione
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    // BCcrypt
    implementation("org.mindrot:jbcrypt:0.4")
    // JWT
    implementation("com.auth0:java-jwt:4.2.1")
    // Mockito para nuestros test con JUnit 5
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
//    // H2, solo usa una
//    implementation("com.h2database:h2:2.1.214")

}

tasks.test {
    useJUnitPlatform()
}

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "server.Server"
//    }
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}

// Para que funcione el shadowJar
tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "server.Server"
    }
}