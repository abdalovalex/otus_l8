FROM maven:3.8.7-eclipse-temurin-17-alpine as build
WORKDIR /app

COPY pom.xml .

RUN mvn --batch-mode --fail-never dependency:resolve-plugins dependency:resolve --file pom.xml

COPY src src

RUN mvn clean install -DskipTests
RUN cp /app/target/billing-service*.jar /app/target/billing-service.jar

FROM openjdk:17-jdk-alpine

RUN apk update && apk add --no-cache libc6-compat libstdc++
WORKDIR /app
COPY --from=build /app/target/billing-service.jar /app/billing-service.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/billing-service.jar"]
