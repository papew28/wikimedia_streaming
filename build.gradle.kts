plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Kafka client
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    implementation("org.opensearch.client:opensearch-rest-client:2.18.0")
    implementation("org.opensearch.client:opensearch-java:2.6.0")

    // HTTP client
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    // Logging with SLF4J
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")


    // Jackson Databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

tasks.test {
    useJUnitPlatform()
}
