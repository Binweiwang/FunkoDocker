FROM gradle:jdk17 AS build

# Directorio de trabajo
WORKDIR /app

# Copia los archivos necesarios para la compilación
COPY build.gradle.kts .
COPY gradlew .
COPY gradle gradle
COPY src src
COPY data data
COPY cert cert

# Ejecuta el script keys.sh
RUN chmod +x /app/cert/keys.sh && /app/cert/keys.sh

# Compila y construye el proyecto
RUN ./gradlew shadowJar

# Etapa de ejecución
FROM openjdk:17-jdk AS run

# Directorio de trabajo
WORKDIR /app

# Copia los archivos necesarios para la ejecución
COPY src/main/resources/database.properties src/main/resources/
COPY data/* data/
COPY cert/* cert/
COPY src/main/resources/server.properties src/main/resources/
COPY src/main/resources/client.properties src/main/resources/
COPY --from=build /app/build/libs/*.jar my-app.jar

# Ejecuta el jar
ENTRYPOINT ["java","-jar","/app/my-app.jar"]
