plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.0.0"
    id("checkstyle")
}
val springCloudVersion by extra("2025.0.0")

group = "conectaseguros.co"
version = "0.0.1-SNAPSHOT"

springBoot {
    mainClass.set("conectaseguros.co.news_service.NewsServiceApplication")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}


dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.6")
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
	}
	dependencies {
		dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
		dependency("io.github.cdimascio:dotenv-java:3.2.0")
		dependency("org.projectlombok:lombok:1.18.42")
        dependency("org.apache.poi:poi:5.4.1")
        dependency("org.apache.poi:poi-ooxml:5.4.1")
        dependency("org.apache.poi:poi-scratchpad:5.4.1")
        dependency("com.opencsv:opencsv:5.12.0")
        dependency("org.jetbrains:annotations:26.0.2-1")
        dependency("io.projectreactor.netty:reactor-netty-http:1.3.0-M7")
        dependency("org.apache.httpcomponents.client5:httpclient5:5.5.1")
	}
}

dependencies {
	// Spring Boot dependencies
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

	// Redis Cache
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Annotations and utilities
	implementation("org.projectlombok:lombok")
    implementation("org.jetbrains:annotations")

	// Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

	// Dotenv support
	implementation("io.github.cdimascio:dotenv-java")

    // Apache POI for Excel file handling
    implementation("org.apache.poi:poi")
    implementation("org.apache.poi:poi-ooxml")
    implementation("org.apache.poi:poi-scratchpad")

    // OpenCSV for CSV file handling
    implementation("com.opencsv:opencsv")

    // Reactor Netty for reactive HTTP support
    implementation("io.projectreactor.netty:reactor-netty-http")

    // Resilience4j for circuit breaking
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // Apache HttpClient for HTTP requests
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // Monitoring dependencies
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Database
	runtimeOnly("org.postgresql:postgresql")

	// Development Dependencies
	"developmentOnly"("org.springframework.boot:spring-boot-devtools")

	// Lombok for compile-time annotation processing
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Testing dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.28.0")
        target("src/**/*.java")
    }
}

checkstyle {
    toolVersion = "11.1.0"
    configFile = file("config/checkstyle/checkstyle.xml")
}
