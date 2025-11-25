# Etapa de construcción
FROM gradle:8.11-jdk21 AS build

WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Copiar código fuente
COPY src src/

# Construir la aplicación (usando la toolchain de Java 21 disponible)
RUN ./gradlew build -x test --no-daemon -Dorg.gradle.java.home=/opt/java/openjdk

# Etapa de ejecución
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copiar el JAR construido desde la etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
