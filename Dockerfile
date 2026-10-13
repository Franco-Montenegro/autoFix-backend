# Imagen del backend de AutoFix (Fase 11.1).
# Build:  docker build -t autofix-backend .
# La configuracion se entrega solo por variables de entorno al ejecutar el contenedor
# (DB_HOST, DB_USERNAME, DB_PASSWORD, KEYCLOAK_ISSUER_URI, CORS_ALLOWED_ORIGINS, ...).
# Las pruebas no se ejecutan aqui: las corre el pipeline (./mvnw verify) antes de construir la imagen.

# ---- Etapa 1: compilacion con JDK 17 y el Maven wrapper ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Primero solo el wrapper y el pom: las dependencias quedan en una capa cacheada
# que se reutiliza mientras el pom.xml no cambie.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests \
    && cp target/backend-*.jar app.jar

# ---- Etapa 2: ejecucion con JRE 17, sin herramientas de compilacion ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario sin privilegios para ejecutar la aplicacion
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring

COPY --from=build --chown=spring:spring /workspace/app.jar app.jar
USER spring

# La JVM usa como maximo el 75% de la memoria asignada al contenedor (se puede sobrescribir en compose)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
