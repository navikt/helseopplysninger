FROM maven:3.6.3-jdk-11-slim as build
WORKDIR /tmp/maven

# Resolve dependencies in a intermediate docker layer that can be cached.
COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src .
RUN mvn package

FROM navikt/java:11
COPY --from=build /tmp/maven/target/*.jar app.jar
