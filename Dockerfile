FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first for faster rebuilds.
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Build application jar.
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -r -u 1001 appuser
USER appuser

COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
