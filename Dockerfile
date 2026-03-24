FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

COPY .github/maven-settings.xml /root/.m2/settings.xml

RUN mvn clean package -DskipTests