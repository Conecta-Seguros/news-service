plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}
val springCloudVersion by extra("2025.0.0")

group = "conectaseguros.co"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}


dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
	}
	dependencies {
		dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
		dependency("io.github.cdimascio:dotenv-java:3.2.0")
		dependency("org.projectlombok:lombok:1.18.38")
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

	// Redis Cache
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Annotations and utilities
	implementation("org.projectlombok:lombok")

	// Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

	// Dotenv support
	implementation("io.github.cdimascio:dotenv-java")

	// Database
	runtimeOnly("org.postgresql:postgresql")

	// Development Dependencies
	"developmentOnly"("org.springframework.boot:spring-boot-devtools")

	// Lombok for compile-time annotation processing
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Testing dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
