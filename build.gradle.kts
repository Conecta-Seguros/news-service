plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "conectaseguros.co"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
	mavenCentral()
}


dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.3")
	}
	dependencies {
		dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	}
}

dependencies {
	// Spring Boot dependencies
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-configuration-processor")

	// Cache
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Annotations and utilities
	implementation("org.projectlombok:lombok")

	// Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

	// Database
	runtimeOnly("org.postgresql:postgresql")

	// Testing dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
