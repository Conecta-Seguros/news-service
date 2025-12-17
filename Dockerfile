# ============================================
# STAGE 1: Build Stage
# ============================================
FROM gradle:jdk25-alpine AS builder

LABEL stage=builder
LABEL description="Build stage for News Service"

WORKDIR /build

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

RUN gradle dependencies --no-daemon --stacktrace || return 0

COPY src src/

RUN gradle clean bootJar --no-daemon --stacktrace -x test

RUN ls -la /build/build/libs/ && \
    test -f /build/build/libs/*.jar && \
    echo "✅ JAR file created successfully"

# ============================================
# STAGE 2: Layer Extraction Stage
# ============================================
FROM eclipse-temurin:25-jdk-alpine AS extractor

LABEL stage=extractor
LABEL description="Layer extraction stage for optimized Docker layers"

WORKDIR /extract

COPY --from=builder /build/build/libs/*.jar application.jar

RUN test -f application.jar && \
    echo "✅ JAR file found successfully"

RUN java -Djarmode=layertools -jar application.jar extract --destination extracted/

RUN ls -la extracted/ && \
    test -d extracted/dependencies && \
    test -d extracted/spring-boot-loader && \
    test -d extracted/application && \
    echo "✅ Layers extracted successfully"

# ============================================
# STAGE 3: Final Runtime Image
# ============================================
FROM eclipse-temurin:25-jre-alpine AS runtime

LABEL maintainer="Conecta Seguros <team@conecta-seguros.com>"
LABEL service="news-service"
LABEL description="News Service Microservice"
LABEL version="1.0.0"
LABEL java.version="25"
LABEL gradle.version="9.1.0"
LABEL spring-boot.version="3.5.6"

RUN apk add --no-cache \
    curl \
    wget \
    tzdata \
    && apk upgrade --no-cache \
    && rm -rf /var/cache/apk/*

ENV TZ=America/Bogota
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN addgroup -S -g 1001 spring && \
    adduser -S -u 1001 -G spring spring && \
    mkdir -p /app && \
    mkdir -p /var/conecta-seguros/archivos && \
    chown -R spring:spring /app && \
    chown -R spring:spring /var/conecta-seguros

WORKDIR /app

COPY --from=extractor --chown=spring:spring /extract/extracted/dependencies/ ./
COPY --from=extractor --chown=spring:spring /extract/extracted/spring-boot-loader/ ./
COPY --from=extractor --chown=spring:spring /extract/extracted/snapshot-dependencies/ ./
COPY --from=extractor --chown=spring:spring /extract/extracted/application/ ./

USER spring:spring

HEALTHCHECK --interval=30s \
            --timeout=10s \
            --start-period=120s \
            --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8034/actuator/health || exit 1


ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:ParallelGCThreads=2 \
    -XX:ConcGCThreads=1 \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseZGC \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Dspring.backgroundpreinitializer.ignore=true \
    -Dreactor.netty.ioWorkerCount=4 \
    -Dreactor.netty.pool.maxConnections=500"

# Use Spring Boot's JarLauncher for layered execution
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]