FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --create-home bookverse
COPY --from=build /app/target/bookverse-1.0.0.jar app.jar
RUN mkdir -p /app/uploads/covers && chown -R bookverse:bookverse /app
USER bookverse
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
